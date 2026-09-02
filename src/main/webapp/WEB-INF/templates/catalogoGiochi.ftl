<#import "common/layout.ftl" as layout>
<#import "common/prodottoCard.ftl" as pc>
<#import "common/paginazione.ftl" as pag>
<#import "common/ricerca.ftl" as r>

<@layout.page page_title="Giochi da tavolo - TableCrown" current_page="catalogo" current_subpage="giochi-da-tavolo" breadcrumbs=breadcrumbs>
    <div class="container px-4">
        <h1 class="title">Giochi da tavolo</h1>

        <@r.barraRicerca actionUrl="${base_url}/catalogo/giochi-da-tavolo" query=query!"" />

        <form method="get" action="${base_url}/catalogo/giochi-da-tavolo" class="filtri-giochi">
            <div class="field is-grouped is-grouped-multiline">
                <div class="control">
                    <input class="input" type="number" step="0.01" name="prezzoMin" value="${filtri.prezzoMin!''}" placeholder="Prezzo min">
                </div>
                <div class="control">
                    <input class="input" type="number" step="0.01" name="prezzoMax" value="${filtri.prezzoMax!''}" placeholder="Prezzo max">
                </div>
                <div class="control">
                    <input class="input" type="number" name="giocatoriMin" value="${filtri.giocatoriMin!''}" placeholder="N° giocatori min">
                </div>
                <div class="control">
                    <input class="input" type="number" name="etaMinima" value="${filtri.etaMinima!''}" placeholder="Età minima">
                </div>
                <div class="control">
                    <div class="select">
                        <select name="difficolta">
                            <option value="">Difficoltà</option>
                            <#list difficoltaOptions as d>
                                <option value="${d.value}" <#if filtri.difficolta! == d.value>selected</#if>>${d.label}</option>
                            </#list>
                        </select>
                    </div>
                </div>
            </div>

            <fieldset class="field">
                <legend class="label">Categoria</legend>
                <#list categorieDisponibili as c>
                    <label class="checkbox mr-3">
                        <input type="checkbox" name="categoria" value="${c.value}"
                               <#if filtri.categoria?? && filtri.categoria?seq_contains(c.value)>checked</#if>>
                        ${c.label}
                    </label>
                </#list>
            </fieldset>

            <button class="button is-primary" type="submit">Filtra</button>
        </form>

        <div class="card-row-vector">
            <#list prodotti as p>
                <@pc.card p=p urlBase="${base_url}/catalogo/giochi-da-tavolo" mostraAggiungiCarrello=true />
            <#else>
                <p class="empty-section-message">Nessun gioco trovato.</p>
            </#list>
        </div>

        <@pag.paginazione paginaCorrente=paginaCorrente totalePagine=totalePagine
        baseUrl="${base_url}/catalogo/giochi-da-tavolo" />
    </div>
</@layout.page>