<table>
  <tr>
    <th>Title</th>
    <th>Excerpt</th>
    <th>URL</th>
    <th>Blog Name</th>
  </tr>
  <#list trackbackList as t>
    <tr>
      <td>${t.title}</td>
      <td>${t.excerpt}</td>
      <td>${t.url}</td>
      <td>${t.blog_name}</td>
    </tr>
  </#list>
</table>
