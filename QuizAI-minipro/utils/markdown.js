/**
 * 轻量级 Markdown 解析器 - 适配微信小程序 rich-text 组件
 * 支持：标题、粗体、斜体、行内代码、代码块、列表、引用、链接、表格、分割线
 */

// HTML 特殊字符转义
function escapeHtml(str) {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

/**
 * 解析行内元素 - 先识别 Markdown 语法，再对内容做 HTML 转义
 */
function parseInline(text) {
  if (!text) return '';

  const result = [];
  let pos = 0;

  while (pos < text.length) {
    // 1. 行内代码 `code`
    const codeMatch = text.slice(pos).match(/^`([^`]+)`/);
    if (codeMatch) {
      result.push('<code style="background:#f6f8fa;padding:2rpx 6rpx;border-radius:6rpx;font-size:22rpx;color:#476582;border:2rpx solid #e5e7eb;">' + escapeHtml(codeMatch[1]) + '</code>');
      pos += codeMatch[0].length;
      continue;
    }

    // 2. 图片 ![alt](url)
    const imgMatch = text.slice(pos).match(/^!\[([^\]]*)\]\(([^)]+)\)/);
    if (imgMatch) {
      result.push('<img src="' + escapeHtml(imgMatch[2]) + '" alt="' + escapeHtml(imgMatch[1]) + '" style="max-width:100%;border-radius:12rpx;margin:8rpx 0;"/>');
      pos += imgMatch[0].length;
      continue;
    }

    // 3. 链接 [text](url)
    const linkMatch = text.slice(pos).match(/^\[([^\]]+)\]\(([^)]+)\)/);
    if (linkMatch) {
      result.push('<a href="' + escapeHtml(linkMatch[2]) + '" style="color:#3B82F6;text-decoration:underline;">' + escapeHtml(linkMatch[1]) + '</a>');
      pos += linkMatch[0].length;
      continue;
    }

    // 4. 粗体 **text**
    const boldMatch = text.slice(pos).match(/^\*\*(.+?)\*\*/);
    if (boldMatch) {
      result.push('<strong>' + escapeHtml(boldMatch[1]) + '</strong>');
      pos += boldMatch[0].length;
      continue;
    }

    // 5. 斜体 *text*
    const italicMatch = text.slice(pos).match(/^\*(.+?)\*/);
    if (italicMatch) {
      result.push('<em>' + escapeHtml(italicMatch[1]) + '</em>');
      pos += italicMatch[0].length;
      continue;
    }

    // 6. 删除线 ~~text~~
    const delMatch = text.slice(pos).match(/^~~(.+?)~~/);
    if (delMatch) {
      result.push('<del style="color:#9ca3af;">' + escapeHtml(delMatch[1]) + '</del>');
      pos += delMatch[0].length;
      continue;
    }

    // 7. 换行
    if (text[pos] === '\n') {
      result.push('<br/>');
      pos++;
      continue;
    }

    // 8. 普通文本 - 找到下一个 Markdown 特殊字符
    let nextSpecial = text.length;
    const specials = ['`', '*', '~', '[', '!', '\n'];
    for (const ch of specials) {
      const idx = text.indexOf(ch, pos + 1);
      if (idx !== -1 && idx < nextSpecial) {
        nextSpecial = idx;
      }
    }
    result.push(escapeHtml(text.slice(pos, nextSpecial)));
    pos = nextSpecial;
  }

  return result.join('');
}

// 解析代码块
function parseCodeBlock(lang, code) {
  const escaped = escapeHtml(code);
  return '<pre style="background:#f6f8fa;color:#24292e;padding:16rpx 20rpx;border-radius:12rpx;overflow-x:auto;margin:16rpx 0;font-size:22rpx;line-height:1.8;font-family:Menlo,Monaco,Consolas,monospace;white-space:pre-wrap;word-break:break-all;border:2rpx solid #e5e7eb;">' + escaped + '</pre>';
}

// 渲染表格
function renderTable(rows) {
  if (rows.length === 0) return '';

  let html = '<div style="overflow-x:auto;margin:16rpx 0;">';
  html += '<table style="width:100%;border-collapse:collapse;font-size:24rpx;">';

  rows.forEach(function(row, rowIndex) {
    const isHeader = rowIndex === 0;
    const bgStyle = isHeader ? 'background:#f3f4f6;' : (rowIndex % 2 === 0 ? 'background:#f9fafb;' : '');
    html += '<tr style="' + bgStyle + '">';
    row.forEach(function(cell) {
      const tag = isHeader ? 'th' : 'td';
      const style = isHeader
        ? 'padding:12rpx 16rpx;border:2rpx solid #e5e7eb;font-weight:600;text-align:left;color:#111827;'
        : 'padding:10rpx 16rpx;border:2rpx solid #e5e7eb;color:#374151;';
      html += '<' + tag + ' style="' + style + '">' + parseInline(cell) + '</' + tag + '>';
    });
    html += '</tr>';
  });

  html += '</table></div>';
  return html;
}

/**
 * 解析 Markdown 文本为 HTML
 */
function parseMarkdown(text) {
  if (!text) return '';

  const lines = text.split('\n');
  const result = [];
  let i = 0;
  let inCodeBlock = false;
  let codeLang = '';
  let codeLines = [];
  let inTable = false;
  let tableRows = [];

  while (i < lines.length) {
    const line = lines[i];

    // 代码块处理 (``` 或 ~~~)
    if (line.trim().startsWith('```') || line.trim().startsWith('~~~')) {
      if (inCodeBlock) {
        result.push(parseCodeBlock(codeLang, codeLines.join('\n')));
        codeLines = [];
        codeLang = '';
        inCodeBlock = false;
      } else {
        inCodeBlock = true;
        codeLang = line.trim().replace(/^(`{3,}|~{3,})/, '').trim();
      }
      i++;
      continue;
    }

    if (inCodeBlock) {
      codeLines.push(line);
      i++;
      continue;
    }

    const trimmed = line.trim();

    // 空行
    if (!trimmed) {
      if (inTable) {
        result.push(renderTable(tableRows));
        tableRows = [];
        inTable = false;
      }
      i++;
      continue;
    }

    // 分割线
    if (/^(-{3,}|_{3,}|\*{3,})$/.test(trimmed)) {
      result.push('<hr style="border:none;border-top:2rpx solid #e5e7eb;margin:24rpx 0;"/>');
      i++;
      continue;
    }

    // 标题
    const headingMatch = trimmed.match(/^(#{1,6})\s+(.+)$/);
    if (headingMatch) {
      const level = headingMatch[1].length;
      const sizes = { 1: '34rpx', 2: '32rpx', 3: '30rpx', 4: '28rpx', 5: '26rpx', 6: '26rpx' };
      const fontWeight = level <= 3 ? '700' : '600';
      const marginTop = level === 1 ? '32rpx' : '24rpx';
      result.push('<h' + level + ' style="font-size:' + sizes[level] + ';font-weight:' + fontWeight + ';color:#111827;margin:' + marginTop + ' 0 12rpx 0;">' + parseInline(headingMatch[2]) + '</h' + level + '>');
      i++;
      continue;
    }

    // 引用块
    if (trimmed.startsWith('>')) {
      const quoteLines = [];
      while (i < lines.length && lines[i].trim().startsWith('>')) {
        quoteLines.push(lines[i].trim().replace(/^>\s?/, ''));
        i++;
      }
      result.push('<blockquote style="border-left:6rpx solid #d1d5db;padding:10rpx 16rpx;margin:16rpx 0;background:#f9fafb;color:#6b7280;font-size:26rpx;line-height:1.8;border-radius:0 12rpx 12rpx 0;">' + parseInline(quoteLines.join('\n')) + '</blockquote>');
      continue;
    }

    // 无序列表
    if (/^[-*+]\s+/.test(trimmed)) {
      const content = trimmed.replace(/^[-*+]\s+/, '');
      result.push('<div style="display:flex;align-items:flex-start;margin:6rpx 0;"><span style="margin-right:10rpx;color:#6b7280;flex-shrink:0;">•</span><span style="font-size:26rpx;color:#1f2937;line-height:1.8;">' + parseInline(content) + '</span></div>');
      i++;
      continue;
    }

    // 有序列表
    const olMatch = trimmed.match(/^(\d+)\.\s+(.+)/);
    if (olMatch) {
      result.push('<div style="display:flex;align-items:flex-start;margin:6rpx 0;"><span style="margin-right:10rpx;color:#6b7280;flex-shrink:0;min-width:32rpx;">' + olMatch[1] + '.</span><span style="font-size:26rpx;color:#1f2937;line-height:1.8;">' + parseInline(olMatch[2]) + '</span></div>');
      i++;
      continue;
    }

    // 表格行
    if (trimmed.includes('|') && trimmed.startsWith('|')) {
      if (/^\|[\s\-:|]+\|$/.test(trimmed)) {
        i++;
        continue;
      }
      inTable = true;
      const cells = trimmed.split('|').filter(function(c) { return c.trim() !== ''; }).map(function(c) { return c.trim(); });
      tableRows.push(cells);
      i++;
      continue;
    }

    // 普通段落
    result.push('<p style="font-size:26rpx;color:#1f2937;line-height:1.9;margin:8rpx 0;">' + parseInline(trimmed) + '</p>');
    i++;
  }

  // 处理未关闭的代码块
  if (inCodeBlock && codeLines.length > 0) {
    result.push(parseCodeBlock(codeLang, codeLines.join('\n')));
  }

  // 处理未关闭的表格
  if (inTable && tableRows.length > 0) {
    result.push(renderTable(tableRows));
  }

  return result.join('');
}

module.exports = {
  parseMarkdown: parseMarkdown
};
