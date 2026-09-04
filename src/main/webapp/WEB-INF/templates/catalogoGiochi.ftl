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
                    <select name="difficolta" onchange="this.form.submit()">
                        <option value="">Tutte</option>
                        <#list difficoltaOptions![] as d>
                            <option value="${d.name()}"
                                    <#if filtri.difficolta?? && filtri.difficolta?seq_contains(d.name())>selected</#if>>
                                ${d}
                            </option>
                        </#list>
                    </select>
                </div>
            </div>
        </div>
    </div>

<#-- Categorie a Pulsantoni -->
    <div class="column is-12 mt-3">
        <div class="field">
            <label class="label has-text-light is-size-7 mb-2">Categorie (Seleziona una o più)</label>
            <div class="control">
                <div class="field is-grouped is-grouped-multiline">
                    <#list categorieDisponibili![] as c>
                        <div class="control" style="margin-bottom: 8px; margin-right: 8px;">
                            <label class="category-pill">
                                <input type="checkbox" name="categoria" value="${c.name()}"
                                       <#if filtri.categoria?? && filtri.categoria?seq_contains(c.name())>checked</#if>>
                                <span class="pill-text">${c}</span>
                            </label>
                        </div>
                    </#list>
                </div>
            </div>
        </div>
    </div>




</@cs.renderCatalogoBase>