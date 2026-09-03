<#macro paginazione paginaCorrente totalePagine baseUrl queryParams="">
<#-- La paginazione appare SOLO se ci sono almeno 2 pagine -->
    <#if totalePagine gt 1>
        <div class="pagination-wrapper">
            <nav class="pagination" role="navigation" aria-label="pagination">

                <#-- Pulsante "Precedente" -->
                <#if paginaCorrente gt 1>
                    <a href="${baseUrl}?page=${paginaCorrente - 1}${queryParams}" class="pagination-previous">
                        <i class="ti ti-chevron-left"></i> Precedente
                    </a>
                </#if>

                <#-- Elenco dei numeri di pagina -->
                <ul class="pagination-list">
                    <#list 1..totalePagine as i>
                        <li>
                            <a href="${baseUrl}?page=${i}${queryParams}"
                               class="pagination-link <#if i == paginaCorrente>is-current</#if>">
                                ${i}
                            </a>
                        </li>
                    </#list>
                </ul>

                <#-- Pulsante "Successiva" -->
                <#if paginaCorrente lt totalePagine>
                    <a href="${baseUrl}?page=${paginaCorrente + 1}${queryParams}" class="pagination-next">
                        Successiva <i class="ti ti-chevron-right"></i>
                    </a>
                </#if>

            </nav>
        </div>
    </#if>
</#macro>