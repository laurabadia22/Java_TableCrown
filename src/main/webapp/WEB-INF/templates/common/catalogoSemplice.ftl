<#import "layout.ftl" as layout>
<#import "prodottoCard.ftl" as pc>
<#import "paginazione.ftl" as pag>
<#import "ricerca.ftl" as r>

<#macro renderCatalogoSemplice titolo subpage urlBase prodotti filtri paginaCorrente totalePagine query breadcrumbs>
    <@layout.page page_title="${titolo} - TableCrown" current_page="catalogo" current_subpage=subpage breadcrumbs=breadcrumbs>
        <div class="container px-4">
            <h1 class="title">${titolo}</h1>

            <@r.barraRicerca actionUrl=urlBase query=query!"" />

            <form method="get" action="${urlBase}" class="filtro-prezzo">
                <div class="field is-grouped">
                    <div class="control">
                        <input class="input" type="number" step="0.01" name="prezzoMin" value="${filtri.prezzoMin!''}" placeholder="Prezzo min">
                    </div>
                    <div class="control">
                        <input class="input" type="number" step="0.01" name="prezzoMax" value="${filtri.prezzoMax!''}" placeholder="Prezzo max">
                    </div>
                    <div class="control">
                        <button class="button is-primary" type="submit">Filtra</button>
                    </div>
                </div>
            </form>

            <div class="card-row-vector">
                <#list prodotti as p>
                    <@pc.card p=p urlBase=urlBase mostraAggiungiCarrello=true />
                <#else>
                    <p class="empty-section-message">Nessun prodotto trovato.</p>
                </#list>
            </div>

            <@pag.paginazione paginaCorrente=paginaCorrente totalePagine=totalePagine baseUrl=urlBase />
        </div>
    </@layout.page>
</#macro>