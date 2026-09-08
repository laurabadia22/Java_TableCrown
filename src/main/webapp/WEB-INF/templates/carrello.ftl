<#import "common/layout.ftl" as layout>
<#import "common/prodottoRiga.ftl" as righe>
<#import "common/prodottoCardVector.ftl" as cardVector>

<#assign extra_css>
    <link rel="stylesheet" href="${base_url}/public/css/prodottoRiga.css">
    <link rel="stylesheet" href="${base_url}/public/css/carrello.css">
    <link rel="stylesheet" href="${base_url}/public/css/home.css">
</#assign>

<@layout.page page_title="Carrello - TableCrown" extra_css=extra_css>

    <div class="carrello-container">
        <div class="container">

            <h1 class="carrello-titolo">
                <i class="ti ti-shopping-cart"></i> Carrello
            </h1>

            <#if carrello_items?? && carrello_items?has_content>

            <#-- ── LAYOUT 2 COLONNE: ARTICOLI + RIEPILOGO ── -->
                <div class="carrello-layout">

                    <#-- COLONNA SINISTRA: ARTICOLI -->
                    <div class="carrello-main">
                        <div class="carrello-items">
                            <#list carrello_items as item>
                                <@righe.riga p=item.prodotto
                                mostraRimuovi=true
                                azioneRimuovi=remove_url
                                iconaRimuovi="ti-trash"
                                quantitaEditabile=true
                                azioneQuantita=update_url
                                quantita=item.quantita
                                prezzoUnitario=item.prezzoUnitario
                                mostraSubtotale=true
                                subtotale=item.subtotale />
                            </#list>
                        </div>
                    </div>

                    <#-- COLONNA DESTRA: RIEPILOGO ORDINE -->
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

                </div> <#-- FINE .carrello-layout -->

            <#-- ── SEZIONE: POTREBBE INTERESSARTI (A PIENA LARGHEZZA) ── -->
                <#if correlati?? && correlati?has_content>
                    <section class="carrello-correlati mt-6">
                        <h2 class="title is-4 has-text-light mb-4 titolo-sezione-custom">Potrebbe interessarti</h2>

                        <div class="card-row-vector">
                            <#list correlati as correlato>
                                <@cardVector.card p=correlato />
                            </#list>
                        </div>
                    </section>
                </#if>

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