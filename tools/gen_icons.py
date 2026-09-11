import os, struct, zlib

BG = (40, 42, 54, 255)
PURPLE = (189, 147, 249, 255)
GREEN = (80, 250, 123, 255)
LINE = (40, 42, 54, 255)
TRANSPARENT = (0, 0, 0, 0)


def new_canvas(size, fill):
    return bytearray(bytes(fill) * (size * size))


def put(buf, size, x, y, color):
    if 0 <= x < size and 0 <= y < size:
        i = (y * size + x) * 4
        buf[i:i + 4] = bytes(color)


def fill_round_rect(buf, size, x0, y0, x1, y1, rad, color):
    for y in range(size):
        py = y + 0.5
        if py < y0 or py >= y1:
            continue
        for x in range(size):
            px = x + 0.5
            if px < x0 or px >= x1:
                continue
            cx = min(max(px, x0 + rad), x1 - rad)
            cy = min(max(py, y0 + rad), y1 - rad)
            dx = px - cx
            dy = py - cy
            if dx * dx + dy * dy <= rad * rad:
                put(buf, size, x, y, color)


def fill_circle(buf, size, rad, color):
    c = size / 2.0
    for y in range(size):
        for x in range(size):
            dx = (x + 0.5) - c
            dy = (y + 0.5) - c
            if dx * dx + dy * dy <= rad * rad:
                put(buf, size, x, y, color)


def draw_motif(buf, size, dx0, dx1, dy0, dy1):
    dw = dx1 - dx0
    dh = dy1 - dy0
    fill_round_rect(buf, size, dx0 * size, dy0 * size, dx1 * size, dy1 * size, dw * size * 0.16, PURPLE)
    lh = dh * 0.10
    lx0 = dx0 + dw * 0.16
    lx1 = dx1 - dw * 0.16
    for fy in (0.26, 0.44, 0.62):
        y0 = (dy0 + dh * fy) * size
        fill_round_rect(buf, size, lx0 * size, y0, lx1 * size, y0 + lh * size, lh * size * 0.4, LINE)
    gy = (dy0 + dh * 0.80) * size
    fill_round_rect(buf, size, lx0 * size, gy, (lx0 + (lx1 - lx0) * 0.55) * size, gy + lh * size, lh * size * 0.4, GREEN)


def write_png(path, size, buf):
    raw = bytearray()
    stride = size * 4
    for y in range(size):
        raw.append(0)
        raw += buf[y * stride:(y + 1) * stride]

    def chunk(typ, data):
        out = struct.pack(">I", len(data)) + typ + data
        out += struct.pack(">I", zlib.crc32(typ + data) & 0xffffffff)
        return out

    ihdr = struct.pack(">IIBBBBB", size, size, 8, 6, 0, 0, 0)
    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr)
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)


def main():
    base = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "app", "src", "main", "res")
    dens = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
    for name, size in dens.items():
        d = os.path.join(base, "mipmap-" + name)

        launcher = new_canvas(size, TRANSPARENT)
        fill_round_rect(launcher, size, 0, 0, size, size, size * 0.18, BG)
        draw_motif(launcher, size, 0.28, 0.72, 0.20, 0.80)
        write_png(os.path.join(d, "ic_launcher.png"), size, launcher)

        rnd = new_canvas(size, TRANSPARENT)
        fill_circle(rnd, size, size * 0.5, BG)
        draw_motif(rnd, size, 0.30, 0.70, 0.30, 0.70)
        write_png(os.path.join(d, "ic_launcher_round.png"), size, rnd)

        fg = new_canvas(size, TRANSPARENT)
        draw_motif(fg, size, 0.34, 0.66, 0.28, 0.72)
        write_png(os.path.join(d, "ic_launcher_foreground.png"), size, fg)

    print("icones gerados")


if __name__ == "__main__":
    main()
