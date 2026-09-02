<#macro paginazione paginaCorrente totalePagine baseUrl queryParams="">
    <#if totalePagine gt 1>
        <nav class="paginazione">
            <#if paginaCorrente gt 1>
                <a href="${baseUrl}?pagina=${paginaCorrente - 1}${queryParams}">&laquo; Precedente</a>
            </#if>
            <#list 1..totalePagine as i>
                <a href="${baseUrl}?pagina=${i}${queryParams}"
                   class="<#if i == paginaCorrente>attiva</#if>">${i}</a>
            </#list>
            <#if paginaCorrente lt totalePagine>
                <a href="${baseUrl}?pagina=${paginaCorrente + 1}${queryParams}">Successiva &raquo;</a>
            </#if>
        </nav>
    </#if>
</#macro>