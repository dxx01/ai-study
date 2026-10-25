import DOMPurify from 'dompurify'
import MarkdownIt from 'markdown-it'

// html: false —— 不渲染原始 HTML；linkify 自动识别链接；breaks 保留换行
const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
})

md.renderer.rules.link_open = (tokens, idx, options, _env, self) => {
  tokens[idx].attrSet('target', '_blank')
  tokens[idx].attrSet('rel', 'noopener noreferrer')
  return self.renderToken(tokens, idx, options)
}

/**
 * 把模型返回的 Markdown 渲染成安全 HTML。
 * 先由 markdown-it 渲染，再由 DOMPurify 清洗，避免 XSS。
 */
export function renderMarkdown(text: string): string {
  return DOMPurify.sanitize(md.render(text))
}
