<#import "common/layout.ftl" as layout>

<#assign extra_css>
    <link rel="stylesheet" href="${base_url}/public/css/carrello.css">
</#assign>

<@layout.page page_title="Carrello - TableCrown" extra_css=extra_css>

    <div class="carrello-container">
        <div class="container">

            <h1 class="carrello-titolo">
                <i class="ti ti-shopping-cart"></i> Carrello
            </h1>

            <#if carrello_items?? && carrello_items?has_content>

                <div class="carrello-layout">

                    <#-- ── COLONNA PRINCIPALE: ARTICOLI + CORRELATI ── -->
                    <div class="carrello-main">

                        <div class="carrello-items">
                            <#list carrello_items as item>
                                <#assign p = item.prodotto>

                                <div class="carrello-item">

                                    <a href="${base_url}/prodotto/?id=${p.idProdotto?c}" class="carrello-item-img-link">
                                        <img src="${base_url}/public/img/prodotti/${p.imgProdotto!''}"
                                             alt="${p.nomeProdotto}"
                                             class="carrello-item-img">
                                    </a>

                                    <div class="carrello-item-info">
                                        <a href="${base_url}/prodotto/?id=${p.idProdotto?c}" class="carrello-item-nome">
                                            ${p.nomeProdotto}
                                        </a>

                                        <div class="carrello-item-prezzo-wrapper">
                                            <#if p.sconto.hasSconto()>
                                                <span class="carrello-item-prezzo">€${item.prezzoUnitario?string("0.00")}</span>
                                                <span class="carrello-item-prezzo-old">€${p.prezzo?string("0.00")}</span>
                                                <span class="carrello-item-sconto-badge">
                                                -${p.sconto.sconto?round}%
                                            </span>
                                            <#else>
                                                <span class="carrello-item-prezzo">€${item.prezzoUnitario?string("0.00")}</span>
                                            </#if>
                                        </div>
                                    </div>

                                    <div class="carrello-item-controls">

                                        <form action="${update_url}" method="post" class="carrello-item-form">
                                            <input type="hidden" name="idProdotto" value="${p.idProdotto?c}">

                                            <div class="carrello-item-qty">
                                                <#-- Pulsante Meno: disabilitato se quantita <= 1 -->
                                                <button class="button quantita-btn"
                                                        type="submit"
                                                        name="quantita"
                                                        value="${item.quantita - 1}"
                                                        <#if (item.quantita <= 1)>disabled</#if>
                                                        aria-label="Riduci quantità">
                                                    <i class="ti ti-minus"></i>
                                                </button>

                                                <#-- Mostra la quantità attuale -->
                                                <span class="carrello-qty-valore">${item.quantita}</span>

                                                <#-- Pulsante Più: disabilitato se quantita >= p.quantita -->
                                                <button class="button quantita-btn"
                                                        type="submit"
                                                        name="quantita"
                                                        value="${item.quantita + 1}"
                                                        <#if (item.quantita >= p.quantita)>disabled</#if>
                                                        aria-label="Aumenta quantità">
                                                    <i class="ti ti-plus"></i>
                                                </button>
                                            </div>

                                            <div class="carrello-item-subtotale">
                                                <span class="carrello-item-subtotale-label">Subtotale</span>
                                                <span class="carrello-item-subtotale-value">€${item.subtotale?string("0.00")}</span>
                                            </div>

                                            <#-- Bottone Rimuovi: sovrascrive l'azione del form inviando a remove_url -->
                                            <button class="carrello-item-rimuovi"
                                                    type="submit"
                                                    formaction="${remove_url}"
                                                    aria-label="Rimuovi ${p.nomeProdotto} dal carrello">
                                                <i class="ti ti-trash"></i> Rimuovi
                                            </button>
                                        </form>

                                    </div>

                                </div>
                            </#list>
                        </div>

                        <#-- ── SEZIONE: POTREBBE INTERESSARTI ──
                             mostraCarrello() non popola ancora l'attributo `correlati`: per
                             attivarla basta chiamare BaseController.prodottiCorrelati(em,
                             List.copyOf(carrelloMap.keySet())) e metterla come request
                             attribute "correlati". Fino ad allora la sezione resta nascosta
                             (##if difensivo). Il carosello JS è sostituito da uno scroll
                             orizzontale CSS scroll-snap. -->
                        <#if correlati?? && correlati?has_content>
                            <section class="carrello-correlati">
                                <h2 class="carrello-section-title">Potrebbe interessarti</h2>

                                <div class="correlati-grid correlati-grid-scrollsnap">
                                    <#list correlati as correlato>
                                        <div class="correlato-card">
                                            <a href="${base_url}/prodotto/?id=${correlato.idProdotto?c}" class="correlato-card-link">
                                                <div class="correlato-image-wrapper">
                                                    <img src="${base_url}/public/img/prodotti/${correlato.imgProdotto!''}"
                                                         alt="${correlato.nomeProdotto}"
                                                         class="correlato-image">
                                                </div>
                                                <div class="correlato-info">
                                                    <h3 class="correlato-nome">${correlato.nomeProdotto}</h3>

                                                    <div class="correlato-rating">
                                                        <#assign cMedia = correlato.valutazioneMedia>
                                                        <#list 1..5 as s>
                                                            <#if s <= cMedia>
                                                                <i class="ti ti-star-filled"></i>
                                                            <#elseif (s - cMedia) < 1>
                                                                <i class="ti ti-star-half-filled"></i>
                                                            <#else>
                                                                <i class="ti ti-star"></i>
                                                            </#if>
                                                        </#list>
                                                    </div>

                                                    <div class="correlato-prezzo">
                                                        <#if correlato.sconto.hasSconto()>
                                                            <span class="correlato-prezzo-scontato">€${correlato.prezzoScontato?string("0.00")}</span>
                                                            <span class="correlato-prezzo-old">€${correlato.prezzo?string("0.00")}</span>
                                                        <#else>
                                                            <span>€${correlato.prezzo?string("0.00")}</span>
                                                        </#if>
                                                    </div>
                                                </div>
                                            </a>

                                            <#-- Riusa CCarrello.aggiungiAlCarrello(): essendo sincrono fa
                                                 un redirect al referer (questa stessa pagina carrello),
                                                 quindi la riga "si aggiorna" senza bisogno di JS. -->
                                            <form action="${base_url}/carrello/aggiungi" method="post" class="correlato-add-form">
                                                <input type="hidden" name="idProdotto" value="${correlato.idProdotto?c}">
                                                <input type="hidden" name="quantita" value="1">
                                                <button class="button btn-correlato-cart" type="submit"
                                                        aria-label="Aggiungi ${correlato.nomeProdotto} al carrello">
                                                    <i class="ti ti-shopping-cart"></i> Carrello
                                                </button>
                                            </form>
                                        </div>
                                    </#list>
                                </div>
                            </section>
                        </#if>

                    </div>

                    <#-- ── COLONNA DESTRA: RIEPILOGO ORDINE ── -->
                    <aside class="carrello-summary">

                        <h2 class="carrello-summary-title">Totale Carrello</h2>

                        <dl class="carrello-summary-list">
                            <div class="carrello-summary-row">
                                <dt>N° articoli</dt>
                                <dd>${carrello_summary.n_articoli}</dd>
                            </div>

                            <#if (carrello_summary.risparmio!0) gt 0>
                                <div class="carrello-summary-row carrello-summary-risparmio">
                                    <dt>Risparmio</dt>
                                    <dd>-€${carrello_summary.risparmio?string("0.00")}</dd>
                                </div>
                            </#if>
                        </dl>

                        <div class="carrello-summary-totale">
                            <span class="carrello-summary-totale-label">Totale</span>
                            <span class="carrello-summary-totale-value">
                            €${carrello_summary.totale?string("0.00")}
                        </span>
                        </div>

                        <a href="${base_url}/checkout" class="button btn-completa-ordine">
                            <i class="ti ti-shopping-cart"></i> Completa Ordine
                        </a>

                    </aside>

                </div>

            <#else>

            <#-- ── CARRELLO VUOTO ── -->
                <div class="carrello-vuoto">
                    <i class="ti ti-shopping-cart-off"></i>
                    <p>Il tuo carrello è vuoto.</p>
                    <a href="${base_url}/catalogo/giochi-da-tavolo" class="btn-primary">Vai al Catalogo</a>
                </div>

            </#if>

        </div>
    </div>

</@layout.page>