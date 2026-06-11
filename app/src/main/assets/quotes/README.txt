Bundled offline quote packs for the Daily Quote feature.

anime.json
  Short, widely-known quotes from anime series, each attributed to its character and
  show. Short quotations with attribution are used here in the spirit of fair use /
  fair dealing; no affiliation with or endorsement by the rights holders is implied.

motivation.json
  Quotations from public-domain authors (ancient Stoics, classical authors, and writers
  whose relevant works predate 1928), plus proverbs. Entries marked "(attributed)" are
  popularly associated with the named person but lack a verified primary source.

Selection is deterministic: index = epochDay % pack.size (see OfflineQuotePack.kt), so
everyone sees the same quote for a given day without any network access.
