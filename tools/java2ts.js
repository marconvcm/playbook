#!/usr/bin/env node
/* eslint-disable no-console */
const fs = require("fs");
const path = require("path");

function usage() {
  console.log(`
Usage:
  node java2ts.js <inputPath> <outputPath>

<inputPath>  : a .java file or a directory
<outputPath> : a .ts file or a directory

Examples:
  node java2ts.js ./src/main/java ./generated/types
  node java2ts.js ./UserDto.java ./UserDto.ts
  node java2ts.js ./UserDto.java ./generated/types
`);
}

function isDirectory(p) {
  try {
    return fs.statSync(p).isDirectory();
  } catch {
    return false;
  }
}
function isFile(p) {
  try {
    return fs.statSync(p).isFile();
  } catch {
    return false;
  }
}

function ensureDir(dirPath) {
  fs.mkdirSync(dirPath, { recursive: true });
}

function listJavaFilesRecursive(dir) {
  const out = [];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) out.push(...listJavaFilesRecursive(full));
    else if (entry.isFile() && entry.name.endsWith(".java")) out.push(full);
  }
  return out;
}

/**
 * Very small "type system"
 */
const primitiveMap = new Map([
  ["byte", "number"],
  ["short", "number"],
  ["int", "number"],
  ["long", "number"],
  ["float", "number"],
  ["double", "number"],
  ["Byte", "number"],
  ["Short", "number"],
  ["Integer", "number"],
  ["Long", "number"],
  ["Float", "number"],
  ["Double", "number"],
  ["BigDecimal", "number"],
  ["BigInteger", "number"],
  ["boolean", "boolean"],
  ["Boolean", "boolean"],
  ["char", "string"],
  ["Character", "string"],
  ["String", "string"],
  ["UUID", "string"],
  ["LocalDate", "string"], // usually ISO date string
  ["LocalDateTime", "string"],
  ["OffsetDateTime", "string"],
  ["ZonedDateTime", "string"],
  ["Instant", "string"],
  ["Date", "string"],
]);

function stripComments(java) {
  // remove /* ... */ and // ...
  return java
    .replace(/\/\*[\s\S]*?\*\//g, "")
    .replace(/\/\/.*$/gm, "");
}

function normalizeWhitespace(s) {
  return s.replace(/\r\n/g, "\n");
}

function splitTopLevelGenerics(typeStr) {
  // split "A<B,C<D>>" inside angle brackets at top level commas
  const parts = [];
  let cur = "";
  let depth = 0;
  for (let i = 0; i < typeStr.length; i++) {
    const ch = typeStr[i];
    if (ch === "<") depth++;
    if (ch === ">") depth--;
    if (ch === "," && depth === 0) {
      parts.push(cur.trim());
      cur = "";
    } else cur += ch;
  }
  if (cur.trim()) parts.push(cur.trim());
  return parts;
}

function removeAnnotations(s) {
  // remove leading annotations like @JsonProperty("x") @NotNull etc
  return s.replace(/(^|\s)@\w+(\([^)]*\))?/g, " ");
}

function cleanTypeName(t) {
  return t
    .trim()
    .replace(/\s+/g, " ")
    .replace(/\bfinal\b/g, "")
    .replace(/\bstatic\b/g, "")
    .replace(/\btransient\b/g, "")
    .replace(/\bvolatile\b/g, "")
    .replace(/\bpublic\b/g, "")
    .replace(/\bprotected\b/g, "")
    .replace(/\bprivate\b/g, "")
    .replace(/\babstract\b/g, "")
    .replace(/\bsealed\b/g, "")
    .replace(/\bnon-sealed\b/g, "")
    .replace(/\bstrictfp\b/g, "")
    .replace(/\s+/g, " ")
    .trim();
}

function javaTypeToTs(javaTypeRaw) {
  let t = cleanTypeName(javaTypeRaw);

  // arrays: Foo[] or Foo[][]
  const arraySuffix = t.match(/(\[\])+$/);
  let arrayDims = 0;
  if (arraySuffix) {
    arrayDims = (arraySuffix[0].match(/\[\]/g) || []).length;
    t = t.replace(/(\[\])+$/g, "").trim();
  }

  // handle wildcard
  t = t.replace(/\? extends\s+/g, "");
  t = t.replace(/\? super\s+/g, "");
  t = t.replace(/\?/g, "unknown");

  // Optional<T>
  const opt = t.match(/^Optional<(.+)>$/);
  if (opt) {
    const inner = javaTypeToTs(opt[1]);
    // We handle "optional" at property level (with ?), but keep union here too:
    return `${inner} | undefined`;
  }

  // List/Set/Collection => Array
  const listLike = t.match(/^(List|Set|Collection|Iterable)<(.+)>$/);
  if (listLike) {
    const inner = javaTypeToTs(listLike[2]);
    return applyArrays(`${inner}[]`, arrayDims);
  }

  // Map<K,V> => Record<string, V> if K is string-ish, else Record<string, V> anyway
  const mapLike = t.match(/^(Map|HashMap|LinkedHashMap|TreeMap)<(.+)>$/);
  if (mapLike) {
    const args = splitTopLevelGenerics(mapLike[2]);
    const k = args[0] ? javaTypeToTs(args[0]) : "string";
    const v = args[1] ? javaTypeToTs(args[1]) : "unknown";
    // TS Record key must be string|number|symbol, we coerce to string.
    const key = (k === "number" || k === "string") ? k : "string";
    return applyArrays(`Record<${key}, ${v}>`, arrayDims);
  }

  // Generic T
  if (/^[A-Z]\w*$/.test(t) && !primitiveMap.has(t)) {
    // Likely a type parameter or class; keep as-is (user can declare/import)
    return applyArrays(t, arrayDims);
  }

  // Simple generic like Foo<Bar>
  const generic = t.match(/^(\w+)<(.+)>$/);
  if (generic) {
    const base = generic[1];
    const args = splitTopLevelGenerics(generic[2]).map(javaTypeToTs).join(", ");
    const baseMapped = primitiveMap.get(base) || base;
    return applyArrays(`${baseMapped}<${args}>`, arrayDims);
  }

  // primitives / common
  const mapped = primitiveMap.get(t) || t;
  return applyArrays(mapped, arrayDims);
}

function applyArrays(tsType, dims) {
  let out = tsType;
  for (let i = 0; i < dims; i++) out += "[]";
  return out;
}

function findPrimaryTypeName(java) {
  // record Name(...)
  const rec = java.match(/\brecord\s+([A-Z]\w*)\s*\(/);
  if (rec) return rec[1];

  // class Name { ... }
  const cls = java.match(/\bclass\s+([A-Z]\w*)\b/);
  if (cls) return cls[1];

  return null;
}

function extractRecordFields(java) {
  // record User(@JsonIgnore String secret, String name) { ... }
  const m = java.match(/\brecord\s+[A-Z]\w*\s*\(([\s\S]*?)\)\s*\{/);
  if (!m) return [];
  const inside = m[1].trim();
  if (!inside) return [];

  const parts = splitTopLevelGenericsCommas(inside);

  return parts
    .map(p => p.trim())
    .filter(Boolean)
    .map(p => {
      // IMPORTANT: check before removing annotations
      if (/\b@JsonIgnore\b/.test(p)) return null;

      const cleaned = removeAnnotations(p).replace(/\s+/g, " ").trim();
      const tokens = cleaned.split(" ");
      if (tokens.length < 2) return null;

      const name = tokens[tokens.length - 1].trim();
      const type = tokens.slice(0, tokens.length - 1).join(" ").trim();

      return { name, type, optional: false };
    })
    .filter(Boolean);
}

function splitTopLevelGenericsCommas(s) {
  const parts = [];
  let cur = "";
  let depth = 0;
  for (let i = 0; i < s.length; i++) {
    const ch = s[i];
    if (ch === "<") depth++;
    if (ch === ">") depth--;
    if (ch === "," && depth === 0) {
      parts.push(cur);
      cur = "";
    } else cur += ch;
  }
  if (cur.trim()) parts.push(cur);
  return parts;
}


function extractClassFields(java) {
  const fields = [];
  const lines = java.split("\n");

  let ignoreNextMember = false;

  for (let raw of lines) {
    let line = raw.trim();
    if (!line) continue;

    // if annotation is on its own line (common)
    if (/^\s*@JsonIgnore\b/.test(raw)) {
      ignoreNextMember = true;
      continue;
    }

    // skip obvious non-field lines
    if (line.startsWith("import ")) continue;
    if (line.startsWith("package ")) continue;
    if (line.startsWith("return ")) continue;
    if (line.startsWith("}")) continue;

    // If @JsonIgnore is on the same line as the field
    if (/\b@JsonIgnore\b/.test(line)) {
      ignoreNextMember = false; // ignore THIS member, not the next
      continue;
    }

    // We only consider lines ending in ';' and NOT containing '('
    if (line.includes("(")) {
      // if the ignore flag was set, and we hit a method/constructor signature,
      // keep it set so the next member can be skipped too.
      continue;
    }
    if (!line.endsWith(";")) continue;

    // remove annotations from the same line (after ignore checks)
    line = removeAnnotations(line);
    line = line.replace(/\s+/g, " ").trim();

    // remove initializer
    line = line.replace(/\s*=\s*.+;$/, ";");

    // match: [mods] Type name;
    const m = line.match(/^(.+?)\s+([a-zA-Z_]\w*)\s*;$/);
    if (!m) continue;

    if (ignoreNextMember) {
      ignoreNextMember = false;
      continue;
    }

    const left = m[1].trim();
    const name = m[2].trim();

    // avoid constants
    if (/\bstatic\b/.test(left) && /\bfinal\b/.test(left)) continue;

    const type = cleanTypeName(left);
    const optional = /^Optional<.+>$/.test(type);

    fields.push({ name, type, optional });
  }

  return fields;
}

function extractGetterFields(java) {
  // Support @JsonIgnore above the getter OR on the same line.
  const out = [];
  const re = /(?:^[ \t]*@\w+(?:\([^)]*\))?[ \t]*\n)*[ \t]*public\s+([\w<>\[\]\s?,]+)\s+(get|is)([A-Z]\w*)\s*\(\s*\)\s*\{/gm;

  let m;
  while ((m = re.exec(java)) !== null) {
    const matchText = m[0];

    // if any annotation block includes @JsonIgnore, skip this getter
    if (/\b@JsonIgnore\b/.test(matchText)) continue;

    const type = m[1].trim();
    const prefix = m[2];
    const propPascal = m[3];

    // ignore getClass()
    if (prefix === "get" && propPascal === "Class") continue;

    const name = propPascal[0].toLowerCase() + propPascal.slice(1);
    const optional = /^Optional<.+>$/.test(type);

    out.push({ name, type, optional });
  }

  return out;
}

function uniqueByNameKeepFirst(items) {
  const seen = new Set();
  const out = [];
  for (const it of items) {
    if (seen.has(it.name)) continue;
    seen.add(it.name);
    out.push(it);
  }
  return out;
}

function toTsType({ typeName, fields }) {
  const lines = [];
  lines.push(`export type ${typeName} = {`);
  for (const f of fields) {
    const tsType = javaTypeToTs(f.type);
    const optMark = f.optional ? "?" : "";
    lines.push(`  ${f.name}${optMark}: ${tsType};`);
  }
  lines.push(`};`);
  lines.push("");
  return lines.join("\n");
}

function convertJavaSource(javaSource) {
  const cleaned = normalizeWhitespace(stripComments(javaSource));

  const typeName = findPrimaryTypeName(cleaned);
  if (!typeName) return null;

  const isRecord = /\brecord\s+/.test(cleaned);

  let fields = [];
  if (isRecord) {
    fields = extractRecordFields(cleaned);
  } else {
    const classFields = extractClassFields(cleaned);
    const getterFields = extractGetterFields(cleaned);
    // prefer explicit fields first, then fill gaps with getters
    fields = uniqueByNameKeepFirst([...classFields, ...getterFields]);
  }

  // mark Optional<T> as optional property (in addition to union in type)
  fields = fields.map(f => ({
    ...f,
    optional: f.optional || /^Optional<.+>$/.test(cleanTypeName(f.type)),
  }));

  return { typeName, ts: toTsType({ typeName, fields }) };
}

function resolveOutputPath(inputFile, inputRoot, outputPath) {
  // If outputPath is a directory: mirror structure, replace .java -> .ts
  // If outputPath is a file: use that exact file for single input
  const outIsDir = isDirectory(outputPath) || outputPath.endsWith(path.sep) || (!path.extname(outputPath) && !outputPath.endsWith(".ts"));
  if (!outIsDir) return outputPath;

  const rel = inputRoot ? path.relative(inputRoot, inputFile) : path.basename(inputFile);
  const relTs = rel.replace(/\.java$/i, ".ts");
  return path.join(outputPath, relTs);
}

function main() {
  const [, , inputPath, outputPath] = process.argv;
  if (!inputPath || !outputPath) {
    usage();
    process.exit(1);
  }

  const absIn = path.resolve(process.cwd(), inputPath);
  const absOut = path.resolve(process.cwd(), outputPath);

  if (!isFile(absIn) && !isDirectory(absIn)) {
    console.error(`Input path not found: ${absIn}`);
    process.exit(2);
  }

  let javaFiles = [];
  let inputRoot = null;

  if (isDirectory(absIn)) {
    inputRoot = absIn;
    javaFiles = listJavaFilesRecursive(absIn);
    if (javaFiles.length === 0) {
      console.error(`No .java files found in directory: ${absIn}`);
      process.exit(3);
    }
  } else {
    javaFiles = [absIn];
  }

  // Decide output mode
  const outIsFile = absOut.endsWith(".ts") && (javaFiles.length === 1);
  if (outIsFile) {
    ensureDir(path.dirname(absOut));
  } else {
    // directory output
    ensureDir(absOut);
  }

  let converted = 0;
  let skipped = 0;

  for (const jf of javaFiles) {
    const java = fs.readFileSync(jf, "utf8");
    const res = convertJavaSource(java);
    if (!res) {
      skipped++;
      continue;
    }

    const outFile = outIsFile
      ? absOut
      : resolveOutputPath(jf, inputRoot, absOut);

    ensureDir(path.dirname(outFile));
    fs.writeFileSync(outFile, res.ts, "utf8");
    converted++;
  }

  console.log(`Done. Converted: ${converted}, Skipped: ${skipped}`);
}

main();
