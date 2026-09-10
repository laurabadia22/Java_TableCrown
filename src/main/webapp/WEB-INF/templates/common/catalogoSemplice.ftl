<#import "layout.ftl" as layout>
<#import "prodottoCard.ftl" as pc>
<#import "paginazione.ftl" as pag>
<#import "ricerca.ftl" as r>

<#macro renderCatalogoBase titolo subpage urlBase prodotti filtri paginaCorrente totalePagine query breadcrumbs totaleRisultati=0>

    <#setting url_escaping_charset="UTF-8">

    <#assign cssCatalogo>
        <link rel="stylesheet" href="${base_url}/public/css/catalogo.css">
        <link rel="stylesheet" href="${base_url}/public/css/home.css">
    </#assign>

<#-- Costruzione query string per la paginazione per non perdere i filtri quando si cambia pagina -->
    <#assign qParams = "">
    <#if query?? && query?has_content>
        <#assign qParams = qParams + "&q=" + query?url>
    </#if>
    <#if filtri??>
        <#if filtri.prezzoMin?? && filtri.prezzoMin?has_content>
            <#assign qParams = qParams + "&prezzoMin=" + filtri.prezzoMin?c>
        </#if>
        <#if filtri.prezzoMax?? && filtri.prezzoMax?has_content>
            <#assign qParams = qParams + "&prezzoMax=" + filtri.prezzoMax?c>
        </#if>
        <#if filtri.giocatoriMin?? && filtri.giocatoriMin?has_content>
            <#assign qParams = qParams + "&giocatoriMin=" + filtri.giocatoriMin>
        </#if>
        <#if filtri.etaMinima?? && filtri.etaMinima?has_content>
            <#assign qParams = qParams + "&etaMinima=" + filtri.etaMinima>
        </#if>
        <#if filtri.ordinamento?? && filtri.ordinamento?has_content>
            <#assign qParams = qParams + "&ordinamento=" + filtri.ordinamento?url>
        </#if>

    <#-- Filtri a lista/sequenza -->
        <#if filtri.difficolta??>
            <#if filtri.difficolta?is_sequence>
                <#list filtri.difficolta as d>
                    <#if d?has_content>
                        <#assign qParams = qParams + "&difficolta=" + d?url>
                    </#if>
                </#list>
            <#elseif filtri.difficolta?has_content>
                <#assign qParams = qParams + "&difficolta=" + filtri.difficolta?url>
            </#if>
        </#if>

        <#if filtri.categoria??>
            <#if filtri.categoria?is_sequence>
                <#list filtri.categoria as c>
                    <#if c?has_content>
                        <#assign qParams = qParams + "&categoria=" + c?url>
                    </#if>
                </#list>
            <#elseif filtri.categoria?has_content>
                <#assign qParams = qParams + "&categoria=" + filtri.categoria?url>
            </#if>
        </#if>
    </#if>

    <@layout.page
    page_title="${titolo} - TableCrown"
    current_page="catalogo"
    current_subpage=subpage
    breadcrumbs=breadcrumbs
    extra_css=cssCatalogo>

        <div class="container section px-4">
            <h1 class="title section-title mb-5">${titolo}</h1>

            <#-- BARRA DI RICERCA E TENDINA ORDINAMENTO SULLA STESSA RIGA -->
            <div class="columns is-vcentered mb-4">

                <#-- Colonna Sinistra: Ricerca -->
                <div class="column is-12-mobile is-7-tablet is-8-desktop">
                    <@r.barraRicerca actionUrl=urlBase query=query!"" />
                </div>

                <#-- Colonna Destra: Ordinamento -->
                <div class="column is-12-mobile is-5-tablet is-4-desktop is-flex justify-content-flex-end">
                    <form method="get" action="${urlBase}" id="form-ordinamento">
                        <#-- Mantiene i filtri correnti quando si cambia l'ordinamento -->
                        <#if query?? && query?has_content>
                            <input type="hidden" name="q" value="${query?html}">
                        </#if>
                        <#if filtri??>
                            <#if filtri.prezzoMin??><input type="hidden" name="prezzoMin" value="${filtri.prezzoMin?c}"></#if>
                            <#if filtri.prezzoMax??><input type="hidden" name="prezzoMax" value="${filtri.prezzoMax?c}"></#if>
                            <#if filtri.giocatoriMin??><input type="hidden" name="giocatoriMin" value="${filtri.giocatoriMin}"></#if>
                            <#if filtri.etaMinima??><input type="hidden" name="etaMinima" value="${filtri.etaMinima}"></#if>

                            <#if filtri.difficolta??>
                                <#if filtri.difficolta?is_sequence>
                                    <#list filtri.difficolta as d><input type="hidden" name="difficolta" value="${d}"></#list>
                                <#else>
                                    <input type="hidden" name="difficolta" value="${filtri.difficolta}">
                                </#if>
                            </#if>
                            <#if filtri.categoria??>
                                <#if filtri.categoria?is_sequence>
                                    <#list filtri.categoria as c><input type="hidden" name="categoria" value="${c}"></#list>
                                <#else>
                                    <input type="hidden" name="categoria" value="${filtri.categoria}">
                                </#if>
                            </#if>
                        </#if>

                        <div class="field is-horizontal align-items-center mb-0">
                            <label class="sort-label-custom">
                                <i class="ti ti-arrows-sort"></i> Ordina:
                            </label>
                            <div class="control">
                                <div class="select sort-select-white">
                                    <select name="ordinamento" onchange="this.form.submit()">
                                        <option value="" <#if !filtri?? || !filtri.ordinamento?? || filtri.ordinamento == "">selected</#if>>Predefinito</option>
                                        <option value="prezzo_asc" <#if filtri?? && filtri.ordinamento?? && filtri.ordinamento == "prezzo_asc">selected</#if>>Prezzo: crescente</option>
                                        <option value="prezzo_desc" <#if filtri?? && filtri.ordinamento?? && filtri.ordinamento == "prezzo_desc">selected</#if>>Prezzo: decrescente</option>
                                        <option value="valutazione" <#if filtri?? && filtri.ordinamento?? && filtri.ordinamento == "valutazione">selected</#if>>Valutazione</option>
                                        <option value="popolarita" <#if filtri?? && filtri.ordinamento?? && filtri.ordinamento == "popolarita">selected</#if>>Più venduti</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>

            </div>

            <#-- BOX FILTRI -->
            <div class="box catalog-filters-box mb-4" style="background-color: var(--color-bg-dark-2); border: 1px solid var(--color-border-dark);">
                <form method="get" action="${urlBase}">
                    <#-- Mantiene la query di ricerca se presente -->
                    <#if query?? && query?has_content>
                        <input type="hidden" name="q" value="${query?html}">
                    </#if>
                    <#-- Mantiene l'ordinamento selezionato quando si applicano i filtri dal box -->
                    <#if filtri?? && filtri.ordinamento?? && filtri.ordinamento?has_content>
                        <input type="hidden" name="ordinamento" value="${filtri.ordinamento}">
                    </#if>

                    <div class="columns is-multiline" style="display: flex; align-items: flex-end;">

                        <#-- Prezzo Minimo -->
                        <div class="column is-6-mobile is-3-tablet is-2-desktop">
                            <label class="label has-text-light font-weight-medium mb-1">Prezzo Min</label>
                            <div class="control has-icons-left">
                                <input class="input filter-input-dark"
                                       type="number"
                                       step="0.01"
                                       min="0"
                                       name="prezzoMin"
                                       placeholder="0"
                                       value="<#if filtri?? && filtri.prezzoMin??>${filtri.prezzoMin?c}</#if>">
                                <span class="icon is-small is-left filter-currency-icon">€</span>
                            </div>
                        </div>

                        <#-- Prezzo Massimo -->
                        <div class="column is-6-mobile is-3-tablet is-2-desktop">
                            <label class="label has-text-light font-weight-medium mb-1">Prezzo Max</label>
                            <div class="control has-icons-left">
                                <input class="input filter-input-dark"
                                       type="number"
                                       step="0.01"
                                       min="0"
                                       name="prezzoMax"
                                       placeholder="Max"
                                       value="<#if filtri?? && filtri.prezzoMax??>${filtri.prezzoMax?c}</#if>">
                                <span class="icon is-small is-left filter-currency-icon">€</span>
                            </div>
                        </div>

                        <#-- Iniezione dei filtri specifici (es. Giochi da tavolo) -->
                        <#nested>

                        <#-- GRUPPO PULSANTI FILTRA E RESETTA (Inglobato dentro .column per rispettare la griglia) -->
                        <div class="column is-12-mobile is-auto">
                            <div class="field is-grouped mb-0">
                                <div class="control">
                                    <button type="submit" class="button is-warning font-weight-bold px-4">
                                        <i class="ti ti-filter mr-1"></i> Filtra
                                    </button>
                                </div>
                                <div class="control">
                                    <a href="${urlBase}" class="button btn-reset-filters">
                                        <i class="ti ti-rotate-clockwise mr-1"></i> Resetta filtri
                                    </a>
                                </div>
                            </div>
                        </div>

                    </div>
                </form>
            </div>

            <#-- CONTEGGIO RISULTATI TOTALI -->
            <div class="results-count-container px-1">
                <span class="results-count-text">
                    <i class="ti ti-box-seam"></i>
                    Trovati <strong>${totaleRisultati}</strong> <#if totaleRisultati == 1>prodotto<#else>prodotti</#if>
                </span>
            </div>

            <#-- GRIGLIA PRODOTTI -->
            <#if prodotti?? && (prodotti?size > 0)>
                <div class="columns is-multiline">
                    <#list prodotti as p>
                        <div class="column is-12-mobile is-6-tablet is-4-desktop flex-card-column">
                            <@pc.card p=p urlBase=urlBase mostraAggiungiCarrello=true />
                        </div>
                    </#list>
                </div>
            <#else>
                <div class="notification is-warning is-light text-center my-6">
                    <i class="ti ti-alert-circle mr-1"></i> Nessun prodotto trovato con i filtri selezionati.
                </div>
            </#if>

            <#-- PAGINAZIONE -->
            <div class="mt-6">
                <@pag.paginazione paginaCorrente=paginaCorrente!1 totalePagine=totalePagine!1 baseUrl=urlBase queryParams=qParams/>
            </div>
        </div>
    </@layout.page>
</#macro>