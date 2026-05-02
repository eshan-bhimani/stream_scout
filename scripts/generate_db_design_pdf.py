from __future__ import annotations

from pathlib import Path

from fpdf import FPDF


def to_latin1_line(line: str) -> str:
  # Keep PDF generation simple/portable: fpdf's core fonts are limited.
  # Replace anything outside latin-1 with '?' to avoid runtime errors.
  return line.encode("latin-1", errors="replace").decode("latin-1")


def chunk_line(line: str, max_chars: int) -> list[str]:
  t = line.rstrip("\n")
  if len(t) <= max_chars:
    return [t]
  chunks: list[str] = []
  start = 0
  while start < len(t):
    end = min(len(t), start + max_chars)
    chunk = t[start:end]

    # Prefer breaking at whitespace when chunking blindly through long tokens
    if end < len(t):
      cut = chunk.rfind(" ")
      if cut > 20:
        chunk = chunk[:cut]
        end = start + cut

    chunks.append(chunk)
    start = end
    while start < len(t) and t[start] == " ":
      start += 1
  return chunks


def main() -> None:
  repo = Path(__file__).resolve().parents[1]
  md = repo / "docs" / "db_design.md"
  out = repo / "docs" / "db_design.pdf"

  pdf = FPDF()
  pdf.set_auto_page_break(auto=True, margin=15)
  pdf.add_page()
  pdf.set_font("Courier", size=10)

  # Courier 10pt: keep chunks conservative to avoid fpdf line-break edge cases
  max_chars = 95

  for raw in md.read_text(encoding="utf-8").splitlines():
    line = to_latin1_line(raw)
    if line.strip() == "":
      pdf.ln(4)
      continue
    for part in chunk_line(line, max_chars=max_chars):
      pdf.multi_cell(0, 5, part)
      pdf.ln(0)

  pdf.output(out.as_posix())
  print(f"Wrote {out}")


if __name__ == "__main__":
  main()
