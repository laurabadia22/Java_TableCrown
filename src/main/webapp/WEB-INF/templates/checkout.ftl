<#import "common/layout.ftl" as layout>

<#assign extra_css>
    <link rel="stylesheet" href="${base_url}/public/css/checkout.css">
</#assign>

<!-- SCRIPT VANILLA JS: Solo per abilitare/disabilitare gli input ed evitare errori di validazione HTML5 quando la carta è nascosta -->
<#assign extra_js>
    <script>
        document.addEventListener('DOMContentLoaded', () => {
            const radioNuova = document.getElementById('radio-nuova-carta');
            const inputs = document.querySelectorAll('.nuova-carta-box input');

            const syncDisabled = () => inputs.forEach(i => i.disabled = !radioNuova.checked);

            document.querySelectorAll('input[name="idCarta"]').forEach(r => r.addEventListener('change', syncDisabled));
            syncDisabled();
        });
    </script>
</#assign>

<@layout.page page_title="Checkout | TableCrown" extra_css=extra_css extra_js=extra_js
breadcrumbs=[
{"label": "Home", "url": "${base_url}/"},
{"label": "Carrello", "url": "${base_url}/carrello"},
{"label": "Checkout", "url": "#"}
]>

    <div class="container py-6">
        <h1 class="title is-3 mb-6">Checkout</h1>

        <form action="${base_url}/checkout" method="POST" id="form-checkout">
            <div class="columns is-variable is-5">

                <!-- COLONNA SINISTRA: Indirizzi e Pagamento -->
                <div class="column is-7">

                    <!-- SEZIONE INDIRIZZI -->
                    <div class="box mb-5">
                        <h2 class="title is-5"><i class="ti ti-map-pin mr-2"></i> Indirizzo di Spedizione</h2>
                        <hr>

                        <#if indirizzi?? && (indirizzi?size > 0)>
                            <#list indirizzi as ind>
                                <label class="radio is-block mb-3 p-4 has-background-light" style="border-radius: 8px; border: 1px solid #eee;">
                                    <input type="radio" name="idIndirizzo" value="${ind.idIndirizzo}" <#if ind.predefinito>checked</#if> required>
                                    <strong>${ind.nome}</strong><br>
                                    ${ind.via}, ${ind.citta} (${ind.provincia}) - ${ind.cap}<br>
                                    ${ind.nazione} <br>
                                    <span class="is-size-7 has-text-grey"><i class="ti ti-bell-ringing"></i> Citofono: ${ind.nomeCitofono}</span>
                                </label>
                            </#list>
                        <#else>
                            <div class="notification is-warning is-light">
                                Non hai ancora salvato un indirizzo di spedizione.
                            </div>
                        </#if>

                        <a href="${base_url}/profilo/indirizzi/nuovo?redirect=checkout" class="button is-small is-link is-outlined mt-2">
                            <i class="ti ti-plus mr-1"></i> Aggiungi nuovo indirizzo
                        </a>
                    </div>

                    <!-- SEZIONE METODI DI PAGAMENTO -->
                    <div class="box">
                        <h2 class="title is-5"><i class="ti ti-credit-card mr-2"></i> Metodo di Pagamento</h2>
                        <hr>

                        <#-- Carte salvate -->
                        <#if carte?? && (carte?size > 0)>
                            <#list carte as carta>
                                <label class="radio is-block mb-3 p-4 has-background-light" style="border-radius: 8px; border: 1px solid #eee;">
                                    <input type="radio" name="idCarta" value="${carta.idCartaDiCredito}" class="radio-carta-esistente" <#if carta.predefinita>checked</#if> required>
                                    <strong>${carta.nomeTitolare}</strong><br>
                                    ${carta.numeroMascherato} <span class="is-size-7 has-text-grey">(Scadenza: ${carta.scadenzaFormattata})</span>
                                </label>
                            </#list>
                        </#if>

                        <#-- Opzione Nuova Carta -->
                        <div class="p-4 has-background-light mb-3" style="border-radius: 8px; border: 1px solid #eee;">
                            <div class="is-flex is-align-items-center">
                                <input type="radio" name="idCarta" value="nuova" id="radio-nuova-carta" <#if !(carte??) || (carte?size == 0)>checked</#if> required>
                                <label for="radio-nuova-carta" class="has-text-weight-bold ml-2" style="cursor: pointer;">
                                    Inserisci una nuova carta
                                </label>
                            </div>

                            <#-- Modulo per Nuova Carta: Visibilità gestita da CSS (#radio-nuova-carta:checked ~ .nuova-carta-box) -->
                            <div class="nuova-carta-box mt-4 p-4 has-background-white-ter" style="border-radius: 8px;">
                                <div class="field">
                                    <label class="label is-small">Titolare Carta</label>
                                    <div class="control has-icons-left">
                                        <input class="input" type="text" name="titolare" placeholder="Es. MARIO ROSSI" required>
                                        <span class="icon is-small is-left"><i class="ti ti-user"></i></span>
                                    </div>
                                </div>
                                <div class="field">
                                    <label class="label is-small">Scadenza (MM/AA)</label>
                                    <div class="control has-icons-left">
                                        <input class="input" type="text" name="scadenza" placeholder="Es. 12/26" pattern="^(0[1-9]|1[0-2])\/\d{2}$" title="Formato richiesto: MM/AA" required>
                                        <span class="icon is-small is-left"><i class="ti ti-calendar"></i></span>
                                    </div>
                                </div>
                                <div class="field mt-3">
                                    <label class="checkbox">
                                        <input type="checkbox" name="salva_carta" value="true">
                                        <span class="is-size-7">Salva questa carta nel mio profilo</span>
                                    </label>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>

                <!-- COLONNA DESTRA: Riepilogo Ordine -->
                <div class="column is-5">
                    <div class="box" style="position: sticky; top: 20px;">
                        <h2 class="title is-5"><i class="ti ti-shopping-cart mr-2"></i> Riepilogo Ordine</h2>
                        <hr>

                        <#if prodotti_carrello?? && (prodotti_carrello?size > 0)>
                            <#list prodotti_carrello as item>
                                <div class="is-flex is-align-items-center mb-4">
                                    <figure class="image is-64x64 mr-3">
                                        <img src="${base_url}/${item.prodotto.imgProdotto}" alt="${item.prodotto.nomeProdotto}" style="object-fit: cover; border-radius: 4px;">
                                    </figure>
                                    <div class="is-flex-grow-1">
                                        <p class="has-text-weight-semibold is-size-6" style="line-height: 1.2;">${item.prodotto.nomeProdotto}</p>
                                        <p class="is-size-7 has-text-grey mt-1">Quantità: ${item.quantita}</p>
                                    </div>
                                    <div class="has-text-right ml-2">
                                        <p class="has-text-weight-bold">€ ${item.subtotale?string("0.00")}</p>
                                    </div>
                                </div>
                            </#list>
                        </#if>

                        <hr>
                        <div class="is-flex is-justify-content-space-between is-align-items-center mb-5">
                            <span class="title is-4 mb-0">Totale</span>
                            <span class="title is-4 mb-0 has-text-primary">€ ${((totale_carrello)!0)?string("0.00")}</span>
                        </div>

                        <button type="submit" class="button is-primary is-fullwidth is-medium" id="btn-conferma">
                            <i class="ti ti-check mr-2"></i> Conferma Ordine
                        </button>
                        <p class="help has-text-centered mt-3">
                            <i class="ti ti-lock"></i> Pagamento sicuro tramite crittografia.
                        </p>
                    </div>
                </div>

            </div>
        </form>
    </div>

</@layout.page>