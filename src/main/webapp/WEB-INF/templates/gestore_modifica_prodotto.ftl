<#import "common/layout.ftl" as layout>

<#-- Dichiariamo eventuale CSS custom se ti serve isolare gli stili -->
<#assign extraCss>
    <style>
        .form-section-title {
            border-bottom: 2px solid var(--color-border-dark);
            padding-bottom: 0.5rem;
            margin-bottom: 1.5rem;
            color: var(--color-text-light);
            font-weight: 600;
        }
        .box-modifica {
            background-color: var(--color-bg-dark-2);
            border: 1px solid var(--color-border-dark);
            border-radius: 8px;
        }
    </style>
</#assign>

<@layout.page
page_title="Modifica Prodotto - TableCrown"
current_page="gestore_dashboard"
extra_css=extraCss>

    <div class="container section px-4">

        <#-- Intestazione e Bottone Indietro -->
        <div class="is-flex is-align-items-center mb-5">
            <a href="${base_url}/gestore/dashboard" class="button is-small is-light mr-4">
                <i class="ti ti-arrow-left mr-1"></i> Indietro
            </a>
            <div>
                <h1 class="title is-3 mb-1 has-text-light">Modifica Prodotto</h1>
                <p class="subtitle is-6 has-text-grey-light">
                    Stai modificando: <strong class="has-text-warning">${prodotto.nomeProdotto}</strong>
                </p>
            </div>
        </div>

        <div class="columns is-centered">
            <div class="column is-8-desktop is-10-tablet">

                <form action="${base_url}/gestore/prodotti/modifica" method="POST" class="box box-modifica p-5">

                    <#-- ID nascosto fondamentale per il backend -->
                    <input type="hidden" name="id_prodotto" value="${prodotto.idProdotto?c}">

                    <#-- ==========================================
                         SEZIONE 1: DISPONIBILITA'
                         ========================================== -->
                    <h2 class="title is-5 form-section-title mt-2">Visibilità Catalogo</h2>
                    <div class="field mb-5">
                        <label class="label has-text-light font-weight-medium">Stato di disponibilità</label>
                        <div class="control">
                            <div class="select is-fullwidth">
                                <select name="disponibilita">
                                    <option value="DISPONIBILE" <#if prodotto.disponibilitaProdotto.name() == 'DISPONIBILE'>selected</#if>>
                                        Disponibile (Pubblico)
                                    </option>
                                    <option value="ESAURITO" <#if prodotto.disponibilitaProdotto.name() == 'ESAURITO'>selected</#if>>
                                        Esaurito (Visibile ma non acquistabile)
                                    </option>
                                    <option value="NON_DISPONIBILE" <#if prodotto.disponibilitaProdotto.name() == 'NON_DISPONIBILE'>selected</#if>>
                                        Nascosto (Solo visibile al Gestore)
                                    </option>
                                </select>
                            </div>
                        </div>
                    </div>

                    <#-- ==========================================
                         SEZIONE 2: SCONTI
                         ========================================== -->
                    <h2 class="title is-5 form-section-title mt-6">Gestione Promozioni</h2>

                    <div class="columns is-multiline">
                        <div class="column is-12">
                            <label class="checkbox mb-3 has-text-light">
                                <input type="checkbox" name="modificaSconto" value="true">
                                <strong class="has-text-info">Applica / Modifica sconto</strong> (spunta per confermare le modifiche ai campi sottostanti)
                            </label>
                        </div>

                        <div class="column is-6">
                            <div class="field">
                                <label class="label has-text-light">Valore Sconto (%)</label>
                                <div class="control has-icons-left">
                                    <#assign valSconto = 0>

                                    <input class="input" type="number" name="valoreSconto" min="0" max="100" step="0.1"
                                           value="${valSconto?c}" placeholder="Es. 15.5">
                                    <span class="icon is-small is-left"><i class="ti ti-percentage"></i></span>
                                </div>
                            </div>
                        </div>

                        <div class="column is-6">
                            <div class="field">
                                <label class="label has-text-light">Scadenza Offerta</label>
                                <div class="control">
                                    <#assign valScadenza = "">
                                    <#if prodotto.sconto?? && prodotto.sconto.scadenzaOfferta??>
                                        <#assign valScadenza = prodotto.sconto.scadenzaOfferta?string('yyyy-MM-dd')>
                                    </#if>
                                    <input class="input" type="date" name="scadenzaOfferta" value="${valScadenza}">
                                </div>
                                <p class="help has-text-grey">Lascia vuoto per sconto illimitato</p>
                            </div>
                        </div>

                        <#-- Opzione per rimuovere brutalmente lo sconto (usando i checkbox eviti il JS) -->
                        <div class="column is-12 mt-2">
                            <label class="checkbox has-text-danger">
                                <input type="checkbox" name="rimuoviSconto" value="true">
                                <strong>Rimuovi promozione attuale</strong> (sovrascrive eventuali modifiche)
                            </label>
                        </div>
                    </div>

                    <#-- ==========================================
                         SEZIONE 3: DANNI (Solo per Giochi da Tavolo)
                         ========================================== -->
                    <#if prodotto.class.simpleName == "EGiocoDaTavolo">
                        <h2 class="title is-5 form-section-title mt-6 has-text-warning">Segnalazione Danni</h2>

                        <div class="notification is-warning is-light mb-4 p-4">
                            <label class="checkbox mb-3 font-weight-bold">
                                <#assign isDanneggiato = (prodotto.danneggiato?? && prodotto.danneggiato)>
                                <input type="checkbox" name="danneggiato" value="true" <#if isDanneggiato>checked</#if>>
                                Segnala questo gioco come danneggiato (spunta per salvare le info sottostanti)
                            </label>

                            <div class="columns is-multiline">
                                <div class="column is-4-tablet">
                                    <div class="field">
                                        <label class="label is-small">Entità del Danno</label>
                                        <div class="control">
                                            <div class="select is-fullwidth is-small">
                                                <select name="livelloDanno">
                                                    <option value="LIEVE" <#if (prodotto.livelloDanno?? && prodotto.livelloDanno.name() == 'LIEVE')>selected</#if>>Lieve</option>
                                                    <option value="MEDIO" <#if (prodotto.livelloDanno?? && prodotto.livelloDanno.name() == 'MEDIO')>selected</#if>>Medio</option>
                                                    <option value="GRAVE" <#if (prodotto.livelloDanno?? && prodotto.livelloDanno.name() == 'GRAVE')>selected</#if>>Grave</option>
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="column is-8-tablet">
                                    <div class="field">
                                        <label class="label is-small">Descrizione (Cosa è rovinato?)</label>
                                        <div class="control">
                                            <input class="input is-small" type="text" name="descrizioneDanno"
                                                   value="${prodotto.descrizioneDanno!''}" placeholder="Es. Angolo della scatola schiacciato">
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </#if>

                    <#-- ==========================================
                         BOTTONI DI SALVATAGGIO
                         ========================================== -->
                    <div class="field is-grouped is-grouped-right mt-6 border-top-dark pt-5">
                        <div class="control">
                            <a href="${base_url}/gestore/dashboard" class="button is-ghost has-text-grey-light">Annulla</a>
                        </div>
                        <div class="control">
                            <button type="submit" class="button is-warning font-weight-bold">
                                <i class="ti ti-device-floppy mr-2"></i> Salva Modifiche
                            </button>
                        </div>
                    </div>

                </form>
            </div>
        </div>
    </div>
</@layout.page>