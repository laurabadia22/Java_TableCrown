<#import "common/catalogoSemplice.ftl" as cs>

<@cs.renderCatalogoBase
titolo="Giochi da tavolo"
subpage="giochi-da-tavolo"
urlBase="${base_url}/catalogo/giochi-da-tavolo"
prodotti=prodotti
filtri=filtri
paginaCorrente=paginaCorrente
totalePagine=totalePagine
query=query!""
breadcrumbs=breadcrumbs>

<#-- Filtri extra specifici per i giochi -->
    <div class="column is-6-mobile is-3-tablet is-2-desktop">
        <div class="field">
            <label class="label has-text-light is-size-7">N° Giocatori Min</label>
            <div class="control">
                <input class="input" type="number" name="giocatoriMin" value="${(filtri.giocatoriMin!'')}" placeholder="Min.">
            </div>
        </div>
    </div>

    <div class="column is-6-mobile is-3-tablet is-2-desktop">
        <div class="field">
            <label class="label has-text-light is-size-7">Età Minima</label>
            <div class="control">
                <input class="input" type="number" name="etaMinima" value="${(filtri.etaMinima!'')}" placeholder="Anni">
            </div>
        </div>
    </div>

    <div class="column is-12-mobile is-3-tablet is-2-desktop">
        <div class="field">
            <label class="label has-text-light is-size-7">Difficoltà</label>
            <div class="control">
                <div class="select is-fullwidth">
                    <select name="difficolta">
                        <option value="">Tutte</option>
                        <#list difficoltaOptions![] as d>
                            <option value="${d.value}" <#if (filtri.difficolta!'') == d.value>selected</#if>>${d.label}</option>
                        </#list>
                    </select>
                </div>
            </div>
        </div>
    </div>

</@cs.renderCatalogoBase>