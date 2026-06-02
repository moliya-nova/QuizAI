"""文章切分工具 — 按字数切分普通文章

直接按字符数切分（chunk_size=500, overlap=50），保留父子块结构：
- 父块：完整文章（存入 MongoDB）
- 子块：切分后的小段（存入 ChromaDB，通过 parent_id 回溯父块）
"""

import hashlib

from langchain_text_splitters import RecursiveCharacterTextSplitter

from quizai_agent.core.settings import settings
from quizai_agent.core.logger import get_logger

logger = get_logger(__name__)


def compute_md5(text: str) -> str:
    return hashlib.md5(text.encode("utf-8")).hexdigest()


def split_article(
    article_id: int,
    title: str,
    url: str,
    content: str,
    content_hash: str | None = None,
) -> tuple[str, list[dict]]:
    if content_hash is None:
        content_hash = compute_md5(content)

    parent_id = f"art_{article_id}_{content_hash[:8]}"

    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=settings.chunk_size,
        chunk_overlap=settings.chunk_overlap,
        length_function=len,
        separators=["\n\n", "\n", "。", "！", "？", ".", "!", "?", "；", ";", "，", ",", " ", ""],
    )

    sub_texts = text_splitter.split_text(content)

    chunks: list[dict] = []
    for i, text in enumerate(sub_texts):
        chunk_hash = compute_md5(text)[:8]
        chunk_id = f"chunk_{article_id}_{i}_{chunk_hash}"
        chunks.append({
            "chunk_id": chunk_id,
            "page_content": text,
            "metadata": {
                "parent_id": parent_id,
                "article_id": article_id,
                "article_title": title,
                "article_url": url,
                "chunk_index": i,
                "content_hash": chunk_hash,
            },
        })

    logger.info(f"文章 {article_id} 切分完成: {len(chunks)} 个子块, parent_id={parent_id}")
    return parent_id, chunks
