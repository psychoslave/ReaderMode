# ReaderMode

An IntelliJ-platform plugin that displays source code in a **reader view**:
tokens are visually replaced by more natural words and forms — without touching
the underlying source file.

Toggled via **View → Reader Mode**.  
Tested on PhpStorm 2024.3; the plugin also registers for Java, Kotlin, PHP,
and web languages (JavaScript/TypeScript/JSX/TSX)
(optional dependencies, loaded only when the respective language plugin is
present).

---

## Features

### Compound identifiers → middot form

Any identifier written in a common multi-word naming convention is replaced
by its lowercase, middle-dot–separated reading:

| Source token       | Reader view        |
|--------------------|--------------------|
| `camelCase`        | `camel·case`       |
| `PascalCase`       | `pascal·case`      |
| `snake_case`       | `snake·case`       |
| `SCREAMING_SNAKE`  | `screaming·snake`  |
| `kebab-case`       | `kebab·case`       |
| `HTMLParser`       | `html·parser`      |
| `parseHTML`        | `parse·html`       |

The middle dot (U+00B7 `·`) was chosen because it is a genuine word-separator
glyph (used in Catalan orthography, ancient Greek manuscripts, etc.), visually
lighter than a hyphen or underscore, and unambiguous in code contexts.

### Variable sigil → "lo-"

The `$` prefix on PHP variables is rendered as the connecting prefix **lo-**:

| Source            | Reader view              |
|-------------------|--------------------------|
| `$variable`       | `lo-variable`            |
| `$someInstance`   | `lo-some·instance`       |
| `$this`           | `lo-this`                |
| `$$variable`      | `lo-lo-variable`         |

**Rationale — deictic prefix.**  
"Lo!" is a short English interjection meaning *look, here is* — directing
attention to whatever follows, which is precisely the role of `$`: it marks
a name as a reference to a value, pointing you at the thing.  As a prefix
with a trailing hyphen it connects directly to the identifier without a
separating space: `$variable` → `lo-variable`.

**Why `lo-` and not `see-` or `hither-`.**  
`see` was the previous rendering as a standalone word.  Switching to a
hyphenated prefix changes its character: `see-variable` reads as a
compound noun rather than a deictic gesture, and "see" carries too many
unrelated meanings (ecclesiastical see, the verb *to see*) to feel
unambiguous as a prefix.  *Hither-* would be the semantically ideal choice
— a directional adverb meaning "toward here", which maps perfectly to the
idea of a sigil pointing you at a memory location — but at six characters
it is twice the width of `lo-` and adds bulk in dense code.  `lo-` is the
shortest unambiguous English word with the same deictic force, with no
competing meanings and no collision risk in any mainstream language.

**Variable variables chain naturally.**  
For PHP's `$$name` construct (a variable whose name is itself stored in
another variable), the sigils simply repeat:

> `$this->methodName()` → *"**lo-**this **whose** method·name **do** **go**"*

> `$$variable` → *"**lo-lo-**variable"*

### Bracket tokens → English words

Each bracket or brace character is replaced by a short English word:

| Token | Word  | Rhyme pair |
|-------|-------|------------|
| `(`   | `do`  | ↕ both end in **-o** |
| `)`   | `go`  | ↕ |
| `{`   | `tap` | ↕ both end in **-p** |
| `}`   | `hop` | ↕ |

### Subscript brackets → "at" / "ate"

Array subscript brackets are rendered as a matched word pair:

| Token | Word  | Pair |
|-------|-------|------|
| `[`   | `at`  | ↕ both built on **-at** |
| `]`   | `ate` | ↕ |

**Rationale — locative / past pair.**  
`[` opens a positional lookup: you are **at** a given index.  
`]` closes it with **ate** — simple past of *eat* — the lookup consumed
the index and yielded the value.  *Eating* a token is a genuine CS
metaphor: parsers eat input, lookups consume a key to produce a value.

**Minimal pair with `at`.**  
`ate` = `at` + `e` — the same word, vowel lengthened into the past.
The phonetic contrast is unambiguous: `at` /æt/ vs `ate` /eɪt/ — no
collision when reading code aloud.

> `$array[index]` → *"lo-array **at** index **ate**"*

> `$matrix[row][lat]` → *"lo-matrix **at** row **ate** **at** lat **ate**"*

### Expression terminator → "ay"

The `;` statement terminator is rendered as **ay**:

| Token | Word |
|-------|------|
| `;`   | `ay` |

**Rationale — affirmative interjection.**  
"Ay!" signals that something is confirmed, acknowledged, done — matching
the role of `;` as the mark that a statement is complete and execution
moves on. Each statement ends with a nod: *ay*.

**Rationale — shape.**  
The word ends in **y**, whose descending curved tail mirrors the descender
of `;` itself — the same visual grounding that gives bracket pairs their
rhyming endings.

> `$one = 1; $two = 2;` → *"lo-one **here** 1 **ay** lo-two **here** 2 **ay**"*

### String quotes (simple/double) → "raw … sic" / "bid … fin"

For basic quoted string literals, ReaderMode renders opening and closing
delimiters with distinct words:

| Literal kind | Open | Close |
|--------------|------|-------|
| single-quoted | `raw` | `sic` |
| double-quoted | `bid` | `fin` |

**Rationale.**
- **`raw … sic`** marks literal/as-written text: `raw` signals verbatim mode,
  `sic` closes with the explicit "thus written" cue.
- **`bid … fin`** marks spoken/interpreted text: `bid` (utter/say) opens the
  quoted utterance, `fin` closes it tersely.

All four words are 3 characters, preserving cadence in string-dense code.

> `'status:ok'` → *"raw status:ok sic"*

> `"hello $name"` → *"bid hello lo-name fin"*

### Argument / list separator → "eft"

The `,` separator is rendered as **eft**:

| Token | Word  |
|-------|-------|
| `,`   | `eft` |

**Rationale.**  
*Eft* is an obsolete English adverb meaning **"again; afterwards"** — precisely
what a comma does: it announces that another item follows.  After each
argument, each list element, each clause: *eft*, here comes the next one.

The word was chosen over more obvious candidates (`and`, `then`, `next`,
`or`) because all of those already carry heavy meaning in programming
languages as operators or keywords, creating a risk of semantic collision
when reading code aloud.  *Eft*'s very rarity is a feature: it sits
outside the active vocabulary of any mainstream language, leaving it
unambiguously ours.

> `set(key, value)` → *"set **do** key **eft** value **go**"*

> `$array = [one, two, three]` → *"lo-array **here** **at** one **eft** two **eft** three **ate**"*

### Member-access operator → "whose"

The `->` operator (PHP object member access) is rendered as **whose**:

| Token | Word    |
|-------|---------|
| `->`  | `whose` |

**Rationale — semantic ownership.**  
`->` denotes that a property or method belongs to an object. *Whose* is the
English possessive interrogative — the exact word we use to ask about
ownership. `$instance->property` reads *"lo-instance whose property"*, which maps cleanly
onto the mental model: we are accessing something that the object owns.

**Rationale — prosodic flow.**  
The word slots naturally into spoken code:

> `$variable->methodName()` → *"**lo-**variable **whose** method·name **do** **go**"*

Possession and invocation read as a natural phrase rather than a symbol
interrupt.

#### Word-choice rationale for bracket tokens

The four bracket words were chosen to satisfy several constraints at once:

**Phonetic symmetry within each pair.**  
`do` / `go` share the `-o` ending; `tap` / `hop` share the `-p` ending.
Open and close brackets are thus audibly paired, mirroring the visual
pairing they form on the page.

**Short and percussive.**  
Each word is a single syllable. Deeply nested code like `fob(gob(hob()))` reads
`fob do gob do hob do go go go` — it scans and even has a rhythmic quality when
spoken aloud. Repetitions roll off the tongue: *tap tap tap … hop hop hop.*

**Interpretable as action words.**  
`do` and `go` are everyday imperatives. `tap` evokes an interjection or
onomatopoeia (a light knock on a surface); `hop` evokes a small jump or skip.
This gives each bracket a mild semantic flavour consistent with what brackets
actually *do* in code:
- `(…)` — you *do* something, then *go* on;
- `{…}` — you *tap* into a block, then *hop* out.

**Prosody when vocalised.**  
A function call like `__construct(SomeClass $instance)` reads aloud as:
> *"underscore underscore construct **do** some·class **lo-**instance **go**"*

A class body opening `SomeClass {` reads as:
> *"some·class **tap**"*

### Scope-resolution operator → "whence"

The `::` operator (namespace / class scope resolution) is rendered as **whence**:

| Token | Word     |
|-------|----------|
| `::`  | `whence` |

**Rationale — provenance adverb.**  
*Whence* is the English adverb meaning **"from where"** or **"from which
place"**. That is exactly what `::` expresses: each segment in a
`Namespace::SubNamespace::Class` chain names the place *from which* the next
name is drawn. Reading left to right, every `::` is a step back up the
breadcrumb — *whence* comes this name?

> `Psr\Log::Logger::create()` → *"psr·log **whence** logger **whence** create **do** **go**"*

**Contrast with `whose`.**  
*Whose* (used for `->`) asks about ownership of an instance member; *whence*
asks about static origin in the type hierarchy. The two operators serve
different navigation models, and the two words reflect that distinction.

### Assignment and equality operators → "here" / "par" / "fit"

| Token | Word   | Role |
|-------|--------|------|
| `=`   | `here` | assignment |
| `==`  | `par`  | loose equality — value, with type coercion |
| `===` | `fit`  | strict equality — same value and type |
| `!=`  | `unlike` | not equal |
| `<>`  | `unlike` | not equal (PHP alias of `!=`) |
| `<=>` | `spy` | three-way comparison (spaceship) |

**`=` — "here".**  
*Here* is a deictic adverb — its function is to point: *here is the thing*.
Positioned between a variable name and its assigned value, it acts as a
natural presenter: `lo-result here lo-a` reads as *"lo-result — here —
lo-a"*, introducing the right-hand side to the left-hand name.

**Why deictic?**  
Assignment is fundamentally an act of presentation: a name is bound to a
value that is *here*, now available under that name.  The deictic reading —
*"lo-result: here, lo-a"* — matches that act directly.  It is also
semantically of a piece with `lo-`: the README grounds `lo-` in "Lo! —
*look, here is*", so *here* and `lo-` operate with the same deictic force,
one naming the container, the other presenting the content.

**Ellipsis is optional, not required.**  
Most candidates for this slot only work if you allow an implied word.  *Here*
works both with and without ellipsis:

- zero ellipsis: *"lo-result, here, lo-a"* — the deictic points at the value,
  self-sufficiently
- with ellipsis: *"lo-result here \[becoming\] lo-a"*, *"lo-result here
  \[assigned\] lo-a"*, *"lo-result here \[set to\] lo-a"*

Because the ellipsis is optional and the possible implied verbs are multiple,
the word does not lock the reader into a single philosophical framing of
what `=` means — binding a name, mutating a slot, defining a constant.
*Here* is neutral enough to accommodate all of them.

**Candidates considered and rejected.**

*"by"* — the previous rendering.  The preposition carries genuine
mathematical precision ("x, as defined by y") and costs only 2 characters.
Its weakness is that it never reaches zero-ellipsis readability: as a
standalone infix between two noun phrases, "by" always needs the implied
*"defined"* to parse as prose.  Read silently it passes; read aloud the
listener must supply the missing verb every time.  *Here* makes the same
claim with no debt.

*"now"* — the most immediate temporal adverb for assignment (*"x is now
y"*), and it flows perfectly as prose.  Rejected because `now` is ubiquitous
in PHP as a method name: `Carbon::now()`, `DateTime::now()`, and dozens of
framework equivalents would create a constant semantic collision between
the assignment token and a method identifier when reading code aloud.

*"gets"* / *"becomes"* / *"sets"* — active verbs that flow naturally but
carry an agency connotation, suggesting the variable is an actor performing
an action rather than a passive name receiving a value.  They also carry
keyword or collision risk in various target languages.

*"is"* — the most natural spoken word for assignment (*"x is y"*) and the
cleanest prose.  Unusable: a reserved keyword in Python, Ruby, and several
other languages.

*"soaks"* — the absorption metaphor is directionally correct (the variable
soaks up the value), but the word is an inflected verb (third-person singular
present), creating a grammatical mismatch in the infix slot.  At 5 characters
it is longer than desirable, and the metaphor collapses outside liquid
contexts: *"lo-count soaks 42"* evokes nothing.

*"blot"* — shares the absorption sense but its dominant semantic field is
damage and erasure: *a blot on your record*, *to blot out a word*.
"Blot out" specifically means to obliterate — the exact opposite of
assignment — and that reading would compete with the intended one on every
occurrence.

*"thus"* / *"hence"* — conjunctive adverbs that work well in mathematical
proof prose (*"let x; thus, y"*).  Their structural problem is that
conjunctive adverbs connect *clauses*, not noun phrases.  `lo-x thus lo-a`
reads as two clauses glued together without punctuation — the same ellipsis
debt as "by", but at a higher grammatical level.

*"hither"* — the directional adverb meaning *"to this place, to here"* —
is the most semantically precise of the archaic candidates: the value travels
*hither* to the variable.  It sits naturally in the literary register already
established by *whence* (`::`) and *whose* (`->`), and its directionality is
unambiguous.  It was rejected on length: at 6 characters it is half again the
size of *here* and adds visible bulk in dense assignment chains.  The same
argument against *hither-* as a prefix for `lo-` applies equally here.

*"thence"* — "from that place", pointing at the right-hand side as the
source.  Directionally interesting but archaic and 6 characters; it also
reads as going *away from* a place rather than arriving *at* one, which
inverts the intuitive direction of assignment.

**`==` — "par".**  
*Par* is the English noun meaning "equal level" (*on a par with*, *below par*).
PHP's `==` compares values with type coercion: two things may be at *par* even
if they are not the same type.

**`===` — "fit".**  
*Fit* captures the exact, tight nature of PHP's `===` check: both value and
type must match — like a key fitting a lock.  The contrast with *par* is
deliberate: being on par allows some flexibility; fitting does not.

> `$result = $a == $b` → *"lo-result **here** lo-a **par** lo-b"*

> `$exact = $type === 'string'` → *"lo-exact **here** lo-type **fit** 'string'"*

**`!=` / `<>` — "unlike".**  
Both not-equal forms render as *unlike*.  PHP supports `<>` as a lexical alias
of `!=`, so the reader-mode rendering keeps them semantically identical.
The term follows Englishest's not-equal family (`unlike?`) and reads naturally
in infix position: *"lo-a unlike lo-b"*.

**`<=>` — "spy".**  
The spaceship operator renders as *spy*, following Englishest's three-way
comparison vocabulary (`spy`).  The word is short, distinctive, and reads
cleanly as an infix comparator in sorter/collator code.

### Relational comparison operators → "ere" / "over" / "ben" / "cap"

The four relational operators are rendered as a matched set of short English words:

| Token | Word  | Role |
|-------|-------|------|
| `<`   | `ere` | strictly less than |
| `>`   | `over`| strictly greater than |
| `<=`  | `ben` | less than or equal to |
| `>=`  | `cap` | greater than or equal to |

> `$count >= 0 && $count < $limit`  
> → *"lo-count **cap** 0 as·well·as lo-count **ere** lo-limit"*

> `$score <= $max && $score > $min`  
> → *"lo-score **ben** lo-max as·well·as lo-score **over** lo-min"*

**Vocabulary source — the *Englishest* Ruby gem.**  
The four words are drawn directly from the `Comparable` and `Hash` modules of
the [Englishest](https://github.com/enalsis/englishest) Ruby gem, which
systematically maps Ruby's operator methods to natural English vocabulary.
The ReaderMode project already uses two Englishest terms — `par` (`==`) and
`fit` (`===`) — so extending the same lineage to the relational quartet keeps
the vocabulary internally coherent.

**`<` — "ere".**  
*Ere* is the English preposition/conjunction meaning *before, prior to* — the
exact temporal/ordinal relationship expressed by strict less-than.
Englishest maps `<` to `ere?`, `afore?`, `precede?`, and several other
candidates; `ere` is chosen here because it is monosyllabic (2 chars),
purely syncategorematic (no content meaning beyond *prior*), and sits in the
same archaic-but-legible register as *whence* (`::`) and *whose* (`->`).

**`>` — "over".**  
*Over* is the direct directional antonym of *ere*: if *ere* means *below/before*,
*over* means *above/beyond*.  Englishest lists `over?`, `top?`, `sky?`, and
`excede?` for `>`; `over` is the shortest and most universally understood,
carries no keyword risk in any target language, and pairs phonetically with
*ere* (both are single syllables ending in a vowel).

**`<=` — "ben".**  
*Ben* is a Scots word meaning *inner, interior, inside* — the same word
Englishest uses for the `<=` operator on both `Comparable` and `Hash`
(subset check).  *Inside* captures the inclusive sense of `<=` well: the
left-hand value is *inside* the range bounded above by the right-hand value.
At 3 characters it is the shortest non-trivial candidate, and its register
is consistent with the other archaic picks in this system.  Alternatives
like *under* (5 chars), *below* (5 chars), and *within* (6 chars) were
rejected on length; *at_most* is too long and uses an underscore.

**`>=` — "cap".**  
*Cap* is the English noun for *an upper limit, a ceiling* — the exact
semantic role of the right-hand operand in a `>=` check: the left-hand value
meets or exceeds the *cap*.  Englishest uses `cap?` for `>=` on both
`Comparable` and `Hash` (superset check).  At 3 characters it mirrors *ben*
in length, forming a tight antonym pair: `ben`/`cap` = inside/ceiling.
Alternatives like *above* (5 chars), *upon* (4 chars), and *um*/*umbe*
(archaic, obscure) were considered; `cap` wins on brevity and immediate
intelligibility in English.

**The strict / non-strict distinction.**  
`ere`/`over` (strict, 3–4 chars) and `ben`/`cap` (non-strict, 3 chars)
are visually and phonetically distinct, so the reader can tell at a glance
whether a boundary is inclusive.  The pairs also reflect natural language:
*"ere the limit"* implies exclusion; *"within the cap"* implies inclusion.

**No collision risk.**  
None of the four words is a keyword, built-in method name, or common
identifier in PHP, JavaScript, TypeScript, Java, or Kotlin:
- `ere` is archaic and unused in any mainstream language.
- `over` appears as a variable/method name but never as a reserved keyword.
- `ben` and `cap` are common English words but have no syntactic role in
  any of the target languages.

### Context-sensitive chevrons in tags and templates

The `<` / `>` chevrons are rendered by syntactic context, not by character
alone. This avoids ambiguity between three different roles:

- JSX/XML tag delimiters
- Type-template / generic delimiters
- Relational comparison operators (`<`, `>`, `<=`, `>=`)

| Context | Token | Word |
|---------|-------|------|
| JSX fragment open | `<>` | `withinside` |
| JSX fragment close | `</>` | `herewith` |
| JSX/XML tag start | `<`  | `within` |
| JSX/XML closing-tag start | `</` | `outwith` |
| JSX/XML tag end | `>` | `therewith` |
| JSX/XML self-close | `/>` | `forthwith` |
| Type template open | `<` | `withal` |
| Type template close | `>` | `so` |
| Relational compare | `<` / `>` | `ere` / `over` |

**Why context-sensitive?**  
In modern web code, the same glyphs appear in totally different constructs.
Applying one global replacement causes immediate regressions: JSX tags become
comparison prose, or generic types become XML-like narration. The folding
builder therefore classifies each chevron by PSI context before assigning a
word.

This also disambiguates `<>` safely across languages:
- in JSX fragment context: `<>` → `withinside`
- in PHP comparison context: `<>` → `unlike` (same as `!=`)

**Tag words — the `-with` family.**  
`withinside / herewith / within / outwith / therewith / forthwith` form a coherent lexical family,
keep all four tag cases distinct, and preserve directional semantics:

- enter anonymous fragment scope → `withinside`
- leave anonymous fragment scope → `herewith`

- enter tag scope → `within`
- start leaving scope → `outwith`
- seal the current tag head → `therewith`
- seal and close immediately (empty element) → `forthwith`

> `<>... </>`  
> → *"withinside ... herewith"*

> `<UseClientTranslations voicePageName="payment-ui">`  
> → *"within use·client·translations voice·page·name here \"payment-ui\" therewith"*

> `</UseClientTranslations>`  
> → *"outwith use·client·translations therewith"*

> `<Icon />`  
> → *"within icon forthwith"*

**Template words — prosody-first close marker.**  
For nested generics, closing markers can repeat indefinitely (`>>>>...`).
Using a short, chant-like closer improves spoken rhythm and scanability in
dense type expressions:

- open template → `withal`
- close template → `so`

> `Promise<Result<T>>`  
> → *"promise withal result withal t so so"*

This mirrors the same prosodic design principle used by `do` / `go` for `(` / `)`:
short repeated closures are easier to parse aloud.

### Ternary operator → "should … thereupon … otherwise …"

The `? :` ternary is rendered as a three-part structure:

| Token | Word        | Role |
|-------|-------------|------|
| *(prefix)* | `should`    | W0 — prefixes the condition |
| `?`   | `thereupon` | W1 — introduces the true branch |
| `:`   | `otherwise` | W2 — introduces the false branch |

> `$result = $count > 0 ? $value : $default`  
> → *"lo-result **here** **should** lo-count **par** 0 **thereupon** lo-value **otherwise** lo-default"*

**Rationale — three words, not two.**  
The most natural English correlative for binary alternation is *either … or …*.
That two-word pair was the first candidate considered, but it has a critical
flaw: the project's own rationale for `eft` (`,`) explicitly lists `or` as a
collision risk — it carries too much weight as a boolean operator in every
target language.  Adding W0 resolves this completely.  Once the condition is
framed by a conditional word, the third word can freely be *otherwise* (the
canonical English fallback) without any ambiguity, because no listener hears a
bare *"otherwise"* and mistakes it for a boolean operator.

**`?` — W0 — "should".**  
*Should* is a conditional auxiliary — purely syncategorematic, no referential
content of its own.  "Should condition hold…" is formal but natural English,
and exactly the right framing: it marks what follows as a test whose outcome
determines which branch is taken.  It has no keyword status in PHP, Java, or
Kotlin, and no collision risk as a method or variable name in typical code.
At 6 characters it is the longest of the three words; it earns that length
by doing the heaviest semantic work — framing the entire expression as a
conditional.

**W0 is not a token replacement.**  
*Should* cannot be handled by the simple text-keyed `OPERATOR_WORDS` map,
because the condition appears *before* the `?` token in source order.  A
single left-to-right leaf-visitor would encounter the condition's first token
before knowing it is part of a ternary.  The folding builder therefore runs in
**two phases**: a pre-scan (Phase 1) finds every `?` that belongs to a ternary
expression, walks back to the first significant leaf of its condition subtree,
and records that offset; the main fold-building pass (Phase 2) then injects
*"should "* as a prefix whenever it encounters a leaf at a recorded offset.
Ternary context is detected by inspecting the parent PSI class name
(`"Ternary"` or `"Conditional"` in the name), avoiding hard compile-time
dependencies on language-plugin classes.

**`?` → W1 — "thereupon".**  
*Thereupon* means *"as a direct consequence of that; immediately upon that"* —
the exact relationship between a satisfied condition and its true branch.
It belongs to the same Old English prepositional-adverb family as *whence*
(`::`) and *thence*: the `there-` root signals "upon that", and the whole word
says "the following is what happens upon the condition being met".  Its
9 characters are justified by semantic precision: it is unambiguously a
consequence marker, not a conjunction, not a modal, and carries no collision
risk anywhere.

**`:` → W2 — "otherwise".**  
*Otherwise* is the canonical English word for a conditional fallback — *"in
other circumstances; if not"*.  It is the prose counterpart of the programming
keyword `else`, carries no keyword status in any target language as a standalone
word, and is immediately understood without any specialist vocabulary.
Its 9 characters mirror those of *thereupon*, giving the two branch markers
equal visual weight in the rendered output.

**Nested ternaries.**  
The three words are fully distinct from one another and from every other token
word in the system.  This makes nested ternaries parseable by structural
matching: each *should* pairs with exactly one *thereupon* and one *otherwise*,
functioning like named brackets.

Right-nested depth 2:
> `$a ? ($b ? 'both' : 'only·a') : 'neither'`  
> → *"**should** lo-a **thereupon** **should** lo-b **thereupon** 'both' **otherwise** 'only·a' **otherwise** 'neither'"*

Compare with the `be / then / or` triplet (the shortest serious alternative):
> *"be lo-a then be lo-b then 'both' or 'only·a' or 'neither'"*

In the `be/then/or` version, the two trailing `or` tokens are visually
indistinguishable from a boolean OR chain (`'both' or 'only·a' or 'neither'`).
In the `should/thereupon/otherwise` version, *otherwise otherwise* reads as two
closing structural markers — the nesting is traceable.

**Candidates considered and rejected.**

*`either / or`* (two words, no W0) — the most natural English correlative for
binary alternation.  Rejected because `or` appears alone and unframed between
the two branches: in nested ternaries, repeated `or` is indistinguishable from
boolean OR chains.  The "or" collision was also flagged in the rationale for
`eft` (`,`).

*`aye / nay`* — collision-free and phonetically paired.  Rejected for two
reasons: the archaic nautical register ("all in favour say aye") is too
far-fetched for a conditional expression, and `aye` (/aɪ/) sits too close to
`ay` (/eɪ/, used for `;`) for comfortable audio disambiguation.

*`be / then / or`* and *`say / then / or`* — shorter triplets (2+4+2 and
3+4+2 chars respectively).  Both retain `or` as W2, which collapses in nested
ternaries as described above.  *Be* is archaic and ambiguous without its
complement "it"; *say* is a content verb, not purely syncategorematic.

*`should / then / or`* — correct W0, but still uses `or` as W2.  The nesting
problem persists.

*`whenever / settle / otherwise`* — correct register for W0 and W2, but
*whenever* (8 chars) implies temporal recurrence rather than a single
conditional test, and *settle* (6 chars) is a content verb, not a function
word.

### Colon usages → context-sensitive words

The `:` token is rendered differently depending on its syntactic context:

| Context                | Example                        | Word      | Rationale summary |
|------------------------|--------------------------------|-----------|-------------------|
| Return type            | `function foo(): int`          | `as`      | English ascription: "foo as int". Short, neutral, no collision. |
| Named argument         | `foo(bar: $baz)`               | `by`      | English agentive: "bar by baz". Short, natural, no collision. |
| Block/case start       | `if ($x): ... endif;`<br>`case 1:` | `thereon` | Consequence marker: "if x, thereon ...". Matches ternary W1. |
| Object property        | `{ key: value }`               | `herewith` | English archaic "along with this". Introduces the value bound to a property/type. |
| Type annotation        | `const x: Type`                | `herewith` | Same as object property; both bind a name to a type or value. |
| Label for goto         | `label:`                       | `-tag`    | Suffix: "label-tag". English for a named marker. |

**Rationale for each word choice:**

- **Return type — `as`**: The colon in a return type reads as ascription: *function foo(): int* means "foo as int". `as` is the shortest, most neutral English word for this relationship, and is not a keyword in PHP, Java, or Kotlin in this context. Alternatives like `of`, `is`, or `returns` were rejected for being either ambiguous, too long, or reserved.

- **Named argument — `by`**: The colon in a named argument (e.g., `foo(bar: $baz)`) expresses agency: "bar by baz". `by` is short, natural, and unambiguous. It was previously used for assignment, but is now reserved for this more precise context. Alternatives like `with`, `as`, or `for` were rejected for being ambiguous or colliding with other usages.

- **Block/case start — `thereon`**: The colon after a block header (e.g., `if ($x): ... endif;`) or a case label (e.g., `case 1:`) marks the start of a consequence. `thereon` means "immediately after that" — the same word used for the ternary `?` (W1). This creates a consistent consequence marker across all conditional and case structures. Alternatives like `then`, `so`, or `do` were rejected for being too short, ambiguous, or already used for other tokens.

- **Object property & type annotation — `herewith`**: The colon in object literals (e.g., `{ workspace: url }`) or type declarations (e.g., `const x: Type`, `param: Type`) introduces a value or type bound to a name. `herewith` is an archaic English word meaning "along with this" or "hereby", capturing the binding relationship: the property *herewith* its value, the parameter *herewith* its type. At 8 characters it carries semantic weight, matching the length of `thereupon` (ternary W1) and maintaining the archaic-register lineage of words like *whence* (`::`) and *whose* (`->`). Alternatives like `as` (already used for return types) or `to` (ambiguous with arrow-function operator) were rejected for collision or ambiguity.

- **Label for goto — `-tag`**: The colon after a label (e.g., `label:`) is rendered as the suffix `-tag`, so `label:` becomes `label-tag`. This is the plain English word for a named marker, and is never ambiguous in code. Alternatives like `mark`, `labelled`, or `goto` were rejected for being either too long, too specific, or colliding with keywords.

**Why context-sensitive?**
Colons serve multiple unrelated roles: ascription, agency, block start, object binding, type binding, and labels. Using a single word would create ambiguity and reduce the clarity of the reader-mode view. Context-sensitive rendering ensures each usage is as clear and natural as possible while maintaining the overall archaic-register lineage of the vocabulary.

### Arrow-function tokens → "from" / "to"

The `fn` keyword and `=>` operator are rendered as a directional pair:

| Token | Word   |
|-------|--------|
| `fn`  | `from` |
| `=>`  | `to`   |

**Rationale — directional pair.**  
`fn(…) => expr` declares a mapping: the parameters are the *source*, the
expression is the *destination*.  *From* and *to* are the shortest,
most natural English words for that relationship, and together they read
as a complete directional phrase.

`=>` doubles as the PHP associative-array value separator
(`'key' => 'value'`), where *to* is equally apt: the key maps *to* its
value.

> `->map(fn($item) => $item->getId())`  
> → *"whose map do **from** do lo-item go **to** lo-item whose get·id do go"*

> `['host' => $url, 'port' => $port]`  
> → *"at 'host' **to** lo-url **eft** 'port' **to** lo-port ate"*

### Prefix-negation operator → "non-"

The `!` prefix operator is rendered as **non-**:

| Token | Word    |
|-------|---------|
| `!`   | `non-`  |

**Rationale — living English prefix.**  
*Non-* is the standard English prefix for logical, objective negation — the
exact semantic role of `!` in code.  Unlike *not* (a reserved word in Python,
Ruby, PHP, and Perl), *non-* carries no collision risk in any mainstream
language.  It is a highly productive, fully standard prefix in both British and
American English (Wiktionary lists over 9 000 derived forms), used with nouns,
adjectives, and adverbs alike, with or without a hyphen depending on register.

**The hyphen is deliberate.**  
It makes the prefix relationship visually explicit and ensures that chained
negations — the common boolean-cast idiom `!!$value` — read as a clean chain
rather than two disconnected words:

> `!$valid` → *"non-lo-valid"*

> `!!$value` → *"non-non-lo-value"*

**Spacing rule.**  
`non-` suppresses its own trailing space (the hyphen is the connector) and
also suppresses the *leading* space of the immediately following token, so
chains never render as `non- non- value`.

### Logical AND operator → "as·well·as"

The `&&` operator (logical AND) is rendered as **as·well·as**:

| Token | Word         | Role                |
|-------|--------------|---------------------|
| `&&`  | `as·well·as` | logical conjunction |

**Rationale — natural conjunction, unambiguous, and prose-friendly.**  
*As·well·as* is a natural English phrase expressing conjunction, which fits the logical AND operation in code. The use of middots (·) visually distinguishes it from the plain English phrase, making it clear that it is a structural token replacement. This choice avoids ambiguity with the `and` keyword, which in some languages (such as PHP and Ruby) has different precedence from `&&` and can be a reserved word, leading to confusion if used as a direct replacement.  

*As·well·as* is also longer and more prosodically fluid than a single word, which helps it blend into prose-style code rendering, matching the project's goal of readable, flowing output. It is unambiguous, not a reserved keyword in any mainstream language, and does not risk collision with variable or method names.

**Alternatives considered and rejected:**

- **`and`** — The most direct English equivalent, but in PHP, Ruby, and other languages, `and` is a keyword with different precedence from `&&`, which could cause confusion in prose rendering and when reading code aloud. Using `and` would also risk collision with code that uses `and` as a variable or method name.
- **`along`, `whereover`** — More poetic, but less immediately clear in intent and less likely to be understood as a logical conjunction by readers unfamiliar with the mapping.
- **`&&`** — Retaining the symbol would not fit the reader-mode’s prose goal.

**Summary:**
*As·well·as* is clear, unambiguous, and avoids precedence confusion with the `and` keyword in PHP, Ruby, and other languages. It fits the project's principle of prioritizing correctness and clarity, and supports the goal of rendering code as natural, readable prose.

### Logical OR operator → "slash"

The `||` operator (logical OR) is rendered as **slash**:

| Token | Word    | Role               |
|-------|---------|--------------------|
| `||`  | `slash` | logical disjunction|

**Rationale — inclusive, visual, and unambiguous.**  
*Slash* is a familiar English word for the `/` character, often used in prose and speech to mean "or" or "alternatively" (e.g., "and/or"). It is short, visually distinct, and not a reserved keyword in any mainstream language. Using *slash* for logical OR avoids confusion with the `or` keyword, which, like `and`, has different precedence in PHP, Ruby, and other languages. The word is easy to read and pronounce, and fits the project's prose-oriented style.

**Alternatives considered and rejected:**

- **`or`** — Direct English equivalent, but a reserved keyword with different precedence in many languages, risking confusion and collision.
- **`yea`** — Considered for inclusive or, but reserved here for exclusive or (XOR) to maintain a clear distinction.
- **`||`** — Retaining the symbol would not fit the reader-mode’s prose goal.

**Summary:**
*Slash* is clear, concise, and avoids precedence confusion with the `or` keyword. It is visually and semantically appropriate for inclusive disjunction in code rendered as prose.

### Exclusive OR operator → "yea"

The `xor` operator (boolean exclusive OR in PHP and some other languages) is rendered as **yea**:

| Token | Word   | Role                  |
|-------|--------|-----------------------|
| `xor` | `yea`  | exclusive disjunction |

**Rationale — archaic, distinctive, and expressive.**  
*Yea* is an archaic English word meaning "or even, or more like, nay" — introducing a stronger or more appropriate alternative. This fits the semantics of exclusive or, which selects one or the other, but not both. The word is short, distinctive, and not a reserved keyword in any mainstream language. Using *yea* for XOR avoids confusion with *slash* (inclusive or) and maintains a clear distinction between the two logical operations.

**Alternatives considered and rejected:**

- **`^`** — In PHP and many languages, `^` is the bitwise XOR operator, not boolean XOR. Rendering it as *yea* would be misleading for boolean logic. (No mapping is provided for bitwise XOR.)
- **`or`** — Too ambiguous for exclusive or; reserved for inclusive or in many languages.
- **`xor`** — Retaining the keyword would not fit the reader-mode’s prose goal.

**Summary:**
*Yea* is expressive, unambiguous, and fits the project's goal of rendering code as natural, readable prose, while clearly distinguishing exclusive or from inclusive or. Only the boolean `xor` operator is mapped; bitwise `^` is not mapped.

---

## How it works

The plugin uses IntelliJ's **code-folding** API.  A fold region replaces a
source range with a placeholder string; expanding the fold restores the
original text.  No file is ever written to.

One fold is created per token:

```
Source:   makeSomething   (            arguments          )
Folds:    [make·something] [ do ]  [arguments] [ go]
Reads:     make·something   do     arguments    go
```

The folding builder (`ReaderModeFoldingBuilder`) runs in **two phases** for
each file.  Phase 1 is a lightweight pre-scan that identifies the first token
of every ternary condition — necessary because the condition precedes `?` in
document order and cannot be annotated during a single left-to-right pass.
Phase 2 is the main `PsiRecursiveElementVisitor` over all leaf PSI elements,
which creates fold descriptors and injects the ternary W0 prefix ("should ")
wherever Phase 1 flagged a condition start.  It is registered as
a `lang.foldingBuilder` extension for each supported language via optional
`<depends>` entries in `plugin.xml`, so it degrades gracefully when a language
plugin is absent.

State (enabled / disabled) is persisted across IDE restarts by
`ReaderModeService`, an application-level `PersistentStateComponent`.

The **View → Reader Mode** action immediately collapses or expands all
reader-mode folds in every open editor, so the switch feels instant.

---

## Project structure

```
src/main/kotlin/com/example/readermode/
  TokenRenderer.kt            structural token → word map, padding helpers
  MiddotConverter.kt          identifier word-splitting (regex-based)
  ReaderModeFoldingBuilder.kt PSI visitor, fold descriptor builder
  ReaderModeService.kt        persistent enabled/disabled toggle
  ToggleReaderModeAction.kt   View-menu action

src/main/resources/META-INF/
  plugin.xml                core descriptor
  readermode-java.xml       optional: registers for language="JAVA"
  readermode-kotlin.xml     optional: registers for language="kotlin"
  readermode-php.xml        optional: registers for language="PHP"
  readermode-web.xml        optional: registers for JS/TS/JSX/TSX languages
```
