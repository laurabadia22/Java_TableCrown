<#import "common/layout.ftl" as layout>

<#assign extra_css>
    <link rel="stylesheet" href="${base_url}/public/css/checkout.css">
</#assign>

<#assign extra_js></#assign>

<@layout.page page_title="Checkout | TableCrown" extra_css=extra_css extra_js=extra_js>

    <div class="checkout-container">
        <div class="container">
            <h1 class="checkout-title"><i class="ti ti-shopping-cart mr-2"></i> Checkout</h1>

            <form action="${base_url}/checkout/acquista" method="POST" id="form-checkout">
                <div class="checkout-grid">

                    <!-- COLONNA SINISTRA: Indirizzi e Pagamento -->
                    <div class="checkout-main">

                        <!-- SEZIONE INDIRIZZI -->
                        <div class="checkout-section">
                            <h2 class="checkout-section-title"><i class="ti ti-map-pin mr-2"></i> Indirizzo di Spedizione</h2>

                            <#if indirizzi?? && (indirizzi?size > 0)>
                                <div class="checkout-radio-list">
                                    <#list indirizzi as ind>
                                        <label class="checkout-radio-card">
                                            <input type="radio" name="id_indirizzo" value="${ind.idIndirizzo}" <#if ind.predefinito>checked</#if> required>
                                            <div class="checkout-radio-content">
                                                <p class="checkout-radio-title">
                                                    ${ind.nome}
                                                    <#if ind.predefinito><span class="checkout-badge-predefinito">Predefinito</span></#if>
                                                </p>
                                                <p class="checkout-radio-sub">${ind.via}, ${ind.citta} (${ind.provincia}) - ${ind.cap} · ${ind.nazione}</p>
                                                <p class="checkout-radio-sub"><i class="ti ti-bell-ringing"></i> Citofono: ${ind.nomeCitofono}</p>
                                            </div>
                                        </label>
                                    </#list>
                                </div>
                            <#else>
                                <p class="checkout-empty">Non hai ancora salvato un indirizzo di spedizione.</p>
                            </#if>

                            <a href="${base_url}/profilo/indirizzi" class="checkout-back-link mt-3">
                                <i class="ti ti-plus mr-1"></i> Aggiungi nuovo indirizzo
                            </a>
                        </div>

                        <!-- SEZIONE METODI DI PAGAMENTO -->
                        <div class="checkout-section">
                            <h2 class="checkout-section-title"><i class="ti ti-credit-card mr-2"></i> Metodo di Pagamento</h2>

                            <#-- Il checkout usa solo carte già salvate: niente inserimento carta inline,
                                 così ogni carta referenziata nell'ordine è sempre un'entità già persistita. -->
                            <input type="hidden" name="scelta_carta" value="salvata">

                            <#if carte?? && (carte?size > 0)>
                                <div class="checkout-radio-list">
                                    <#list carte as carta>
                                        <label class="checkout-radio-card">
                                            <input type="radio" name="id_carta_salvata" value="${carta.idCartaDiCredito}" <#if carta.predefinita>checked</#if> required>
                                            <div class="checkout-radio-content">
                                                <p class="checkout-radio-title">
                                                    ${carta.nomeTitolare}
                                                    <#if carta.predefinita><span class="checkout-badge-predefinito">Predefinita</span></#if>
                                                </p>
                                                <p class="checkout-radio-sub">${carta.numeroMascherato} (Scadenza: ${carta.scadenzaFormattata})</p>
                                            </div>
                                        </label>
                                    </#list>
                                </div>
                            <#else>
                                <p class="checkout-empty">Non hai ancora salvato un metodo di pagamento.</p>
                            </#if>

                            <a href="${base_url}/profilo/pagamenti" class="checkout-back-link mt-3">
                                <i class="ti ti-plus mr-1"></i> Aggiungi nuovo metodo di pagamento
                            </a>
                        </div>
                    </div>

                    <!-- COLONNA DESTRA: Riepilogo Ordine -->
                    <div class="checkout-summary-box">
                        <h2 class="checkout-summary-title"><i class="ti ti-shopping-cart mr-2"></i> Riepilogo Ordine</h2>

                        <#if prodotti_carrello?? && (prodotti_carrello?size > 0)>
                            <div class="checkout-prodotti-list">
                                <#list prodotti_carrello as item>
                                    <div class="checkout-prodotto-row">
                                        <img class="checkout-prodotto-img" src="${base_url}/public/img/prodotti/${(item.prodotto.imgProdotto)!''}" alt="${(item.prodotto.nomeProdotto)?html}">
                                        <div class="checkout-prodotto-info">
                                            <p class="checkout-prodotto-nome">${item.prodotto.nomeProdotto}</p>
                                            <p class="checkout-prodotto-qta">Quantità: ${item.quantita}</p>
                                        </div>
                                        <div class="checkout-prodotto-prezzo">€ ${item.subtotale?string("0.00")}</div>
                                    </div>
                                </#list>
                            </div>
                        </#if>

                        <div class="checkout-summary-total">
                            <span>Totale</span>
                            <span>€ ${((totale_carrello)!0)?string("0.00")}</span>
                        </div>

                        <button type="submit" class="btn-checkout-conferma" id="btn-conferma">
                            <i class="ti ti-check mr-2"></i> Conferma Ordine
                        </button>
                        <p class="checkout-secure-note">
                            <i class="ti ti-lock"></i> Pagamento sicuro tramite crittografia.
                        </p>
                    </div>

                </div>
            </form>
        </div>
    </div>

</@layout.page>
