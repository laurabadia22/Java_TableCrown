<#import "layout.ftl" as layout>

<#macro formCreazione actionUrl urlAnnulla titoloPagina currentSubpage titoloForm sottotitoloForm>
    <#assign cssForm>
        <link rel="stylesheet" href="${base_url}/public/css/gestoreDashboard.css">
    </#assign>

    <@layout.page page_title="${titoloPagina} - Gestore" current_page="gestore_${currentSubpage}" extra_css=cssForm>
        <div class="container px-4 mt-6 mb-6 gestore-form">

            <!-- Intestazione con pulsante Indietro (allineata alla Modifica) -->
            <div class="is-flex is-align-items-center mb-5">
                <a href="${base_url}${urlAnnulla}" class="button is-small is-light mr-4">
                    <i class="ti ti-arrow-left mr-1"></i> Indietro
                </a>
                <div>
                    <h1 class="title is-3 mb-1">${titoloForm}</h1>
                    <p class="subtitle is-6">${sottotitoloForm}</p>
                </div>
            </div>

            <!-- Griglia per centrare e restringere il form (come nella Modifica) -->
            <div class="columns is-centered">
                <div class="column is-8-desktop is-10-tablet">

                    <!-- Aggiunta classe box-modifica per ereditare lo stile esatto -->
                    <form action="${base_url}${actionUrl}" method="POST" enctype="multipart/form-data" class="box box-modifica p-5">

                        <h2 class="title is-5 form-section-title mt-2">Informazioni Principali</h2>
                        <div class="columns is-multiline">
                            <div class="column is-8">
                                <div class="field">
                                    <label class="label">Nome Prodotto *</label>
                                    <div class="control">
                                        <input class="input" type="text" name="nomeProdotto" required maxlength="255" placeholder="Inserisci il nome del prodotto...">
                                    </div>
                                </div>
                            </div>

                            <div class="column is-4">
                                <div class="field">
                                    <label class="label">Disponibilità *</label>
                                    <div class="control">
                                        <div class="select is-fullwidth">
                                            <select name="disponibilita" required>
                                                <option value="DISPONIBILE">Disponibile</option>
                                                <option value="ESAURITO">Esaurito</option>
                                                <option value="NON_DISPONIBILE">Nascosto dal catalogo</option>
                                            </select>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="column is-12">
                                <div class="field">
                                    <label class="label">Descrizione *</label>
                                    <div class="control">
                                        <textarea class="textarea" name="descrizioneProdotto" required rows="3" maxlength="2000" placeholder="Descrivi il prodotto..."></textarea>
                                    </div>
                                </div>
                            </div>

                            <div class="column is-4">
                                <div class="field">
                                    <label class="label">Prezzo di Listino (€) *</label>
                                    <div class="control has-icons-left">
                                        <input class="input" type="number" name="prezzoListino" required min="0" step="0.01" placeholder="es. 19.90">
                                        <span class="icon is-small is-left"><i class="ti ti-currency-euro"></i></span>
                                    </div>
                                </div>
                            </div>

                            <div class="column is-4">
                                <div class="field">
                                    <label class="label">Quantità iniziale *</label>
                                    <div class="control has-icons-left">
                                        <input class="input" type="number" name="quantita" required min="0" step="1" value="0">
                                        <span class="icon is-small is-left"><i class="ti ti-package"></i></span>
                                    </div>
                                </div>
                            </div>

                            <div class="column is-4">
                                <div class="field">
                                    <label class="label">Immagine Prodotto</label>
                                    <div class="control">
                                        <input class="input" type="file" name="img_prodotto" accept="image/*">
                                    </div>
                                </div>
                            </div>
                        </div>

                        <#-- QUI VERRANNO INIETTATI I CAMPI SPECIFICI (es. per Giochi da Tavolo) -->
                        <#nested>

                        <h2 class="title is-5 form-section-title mt-6">Promozione (Opzionale)</h2>
                        <div class="columns is-multiline">
                            <div class="column is-12">
                                <label class="checkbox mb-3">
                                    <input type="checkbox" name="scontoAttivo" value="true">
                                    <strong class="has-text-warning">Applica uno sconto iniziale</strong>
                                </label>
                            </div>
                            <div class="column is-6">
                                <div class="field">
                                    <label class="label">Percentuale Sconto (%)</label>
                                    <div class="control has-icons-left">
                                        <input class="input" type="number" name="valoreSconto" min="0" max="100" step="0.1" placeholder="es. 15">
                                        <!-- Aggiunta icona percentuale -->
                                        <span class="icon is-small is-left"><i class="ti ti-percentage"></i></span>
                                    </div>
                                </div>
                            </div>
                            <div class="column is-6">
                                <div class="field">
                                    <label class="label">Scadenza Offerta</label>
                                    <div class="control">
                                        <input class="input" type="date" name="scadenzaOfferta">
                                    </div>
                                    <p class="help has-text-grey">Lascia vuoto per sconto illimitato</p>
                                </div>
                            </div>
                        </div>

                        <!-- Bottoni allineati allo stile della modifica -->
                        <div class="field is-grouped is-grouped-right mt-6 border-top-dark pt-5">
                            <div class="control">
                                <a href="${base_url}${urlAnnulla}" class="button is-ghost has-text-grey-light">Annulla</a>
                            </div>
                            <div class="control">
                                <button type="submit" class="button is-warning font-weight-bold">
                                    <i class="ti ti-device-floppy mr-2"></i> Salva Prodotto
                                </button>
                            </div>
                        </div>

                    </form>

                </div>
            </div>

        </div>
    </@layout.page>
</#macro>