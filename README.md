# ReaderMode

An IntelliJ-platform plugin that displays source code in a **reader view**:
tokens are visually replaced by more natural English words and forms —
without touching the underlying source file.

Toggled via **View → Reader Mode**.

---

## What it does

- Multi-word identifiers (`camelCase`, `snake_case`, …) become middot-separated
  prose: `camel·case`, `snake·case`.
- Structural tokens (`(`, `)`, `{`, `}`, `;`, `,`, `->`, `::`, …) are replaced
  by short English words that reflect their roles.
- Context-sensitive mappings handle ambiguous characters: `<` and `>` render
  differently in JSX tags, generic type parameters, and relational comparisons.
- The ternary `? :` becomes a three-word conditional phrase.
- Colon `:` is rendered differently as a return-type marker, named-argument
  separator, block-start, property binder, or label suffix.

A full list of all token mappings and the reasoning behind every choice is in
[LEXICON.md](LEXICON.md).

---

## Supported languages

PHP, JavaScript, TypeScript, JSX, TSX, Java, Kotlin.

Language support is loaded via optional `<depends>` entries in `plugin.xml` —
the plugin degrades gracefully when a language plugin is absent.

Tested on PhpStorm 2024.3 (IntelliJ platform 2024.3).

---

## Installation

### From source

Requirements: JDK 17+, internet access for Gradle dependency download.

```bash
git clone https://github.com/your-org/ReaderMode.git
cd ReaderMode
./gradlew buildPlugin
```

The plugin ZIP is produced at `build/distributions/ReaderMode-*.zip`.

In your JetBrains IDE:

1. **Settings → Plugins → ⚙ → Install Plugin from Disk…**
2. Select the ZIP file.
3. Restart the IDE.

### Running a sandboxed IDE for development

```bash
./gradlew runIde
```

This launches a fresh PhpStorm instance with the plugin pre-installed.

---

## Usage

Open any supported source file, then:

- **View → Reader Mode** — toggle the reader view on or off.

The toggle is instant and persistent across IDE restarts.

---

## How it works

The plugin uses IntelliJ's **code-folding** API.  A fold region replaces a
source range with a placeholder string; expanding the fold restores the
original text.  No file is ever written to.

```
Source:   makeSomething   (            arguments          )
Folds:    [make·something] [ do ]  [arguments] [ go]
Reads:     make·something   do     arguments    go
```

The folding builder (`ReaderModeFoldingBuilder`) runs in two phases per file:

1. **Phase 1 — pre-scan**: identifies the first token of every ternary
   condition so that the `should` prefix can be injected in document order.
2. **Phase 2 — main pass**: a `PsiRecursiveElementVisitor` over all leaf PSI
   elements that creates fold descriptors and injects prefixes.

State (enabled / disabled) is persisted by `ReaderModeService`, an
application-level `PersistentStateComponent`.

---

## Project structure

```
src/main/kotlin/com/example/readermode/
  TokenRenderer.kt            token → word map, padding helpers
  MiddotConverter.kt          identifier word-splitting (regex-based)
  ReaderModeFoldingBuilder.kt PSI visitor, fold descriptor builder
  ReaderModeService.kt        persistent enabled/disabled toggle
  ToggleReaderModeAction.kt   View-menu action

src/main/resources/META-INF/
  plugin.xml                core descriptor
  readermode-java.xml       optional: registers for language="JAVA"
  readermode-kotlin.xml     optional: registers for language="kotlin"
  readermode-php.xml        optional: registers for language="PHP"
  readermode-web.xml        optional: registers for JS/TS/JSX/TSX
```

---

## Further reading

| File | Contents |
|------|----------|
| [LEXICON.md](LEXICON.md) | Full linguistic rationale for every token-to-word mapping, including all alternatives considered and rejected. |
