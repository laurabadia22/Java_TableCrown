<#import "common/layout.ftl" as layout>

<#assign extra_css>
    <link rel="stylesheet" href="${base_url}/public/css/profiloOrdini.css">
</#assign>

<#assign extra_js>
    <script>
        (function() {
            var list = document.getElementById('mieordini-list');
            if (list) {
                list.addEventListener('click', function(e) {
                    var header = e.target.closest('.mieordini-card-header');
                    if (!header) return;

                    var body = document.getElementById(header.getAttribute('data-target'));
                    if (!body) return;

                    var aperto = body.hasAttribute('hidden');
                    if (aperto) {
                        body.removeAttribute('hidden');
                    } else {
                        body.setAttribute('hidden', '');
                    }
                    header.setAttribute('aria-expanded', aperto ? 'true' : 'false');
                });
            }
        })();
    </script>
</#assign>

<@layout.page page_title="I Miei Ordini - TableCrown" extra_css=extra_css extra_js=extra_js>

    <div class="mieordini-container">
        <div class="container">

            <div class="mieordini-topbar">
                <a href="${base_url}/profilo" class="mieordini-back-link">
                    <i class="ti ti-arrow-left"></i> Torna all'Area Personale
                </a>
            </div>

            <div class="mieordini-header">
                <div class="mieordini-header-text">
                    <span class="mieordini-eyebrow">Area Personale</span>
                    <h1 class="mieordini-titolo">
                        <i class="ti ti-package"></i> I Miei Ordini
                    </h1>
                </div>

                <#if ordiniVista?has_content>
                    <div class="mieordini-count-badge">
                        <span class="mieordini-count-num">${ordiniVista?size}</span>
                        <span class="mieordini-count-label"><#if ordiniVista?size == 1>ordine<#else>ordini</#if></span>
                    </div>
                </#if>
            </div>

            <#if ordiniVista?has_content>
                <div class="mieordini-list" id="mieordini-list">
                    <#list ordiniVista as riga>
                        <#assign ordine = riga.ordine>
                        <div class="mieordini-card" id="mieordini-card-${ordine.idOrdine}">

                            <button type="button" class="mieordini-card-header" data-target="mieordini-body-${ordine.idOrdine}" aria-expanded="false">
                                <div class="mieordini-card-header-left">
                                    <span class="mieordini-ordine-id">Ordine #${ordine.idOrdine}</span>
                                    <span class="mieordini-ordine-data">${riga.dataFormattata}</span>
                                </div>

                                <div class="mieordini-card-header-right">
                                    <span class="mieordini-stato-badge mieordini-stato-${ordine.stato.name()?lower_case}">
                                        <#if ordine.stato.name() == 'IN_LAVORAZIONE'>
                                            <i class="ti ti-clock"></i> In lavorazione
                                        <#elseif ordine.stato.name() == 'SPEDITO'>
                                            <i class="ti ti-truck-delivery"></i> Spedito
                                        <#elseif ordine.stato.name() == 'CONSEGNATO'>
                                            <i class="ti ti-circle-check"></i> Consegnato
                                        <#elseif ordine.stato.name() == 'ANNULLATO'>
                                            <i class="ti ti-circle-x"></i> Annullato
                                        <#else>
                                            ${ordine.stato.name()}
                                        </#if>
                                    </span>

                                    <span class="mieordini-ordine-totale">${ordine.calcolaTotale()?string("0.00")} €</span>

                                    <i class="ti ti-chevron-down mieordini-chevron"></i>
                                </div>
                            </button>

                            <div class="mieordini-card-body" id="mieordini-body-${ordine.idOrdine}" hidden>

                                <div class="mieordini-items">
                                    <#list ordine.ordineItems as item>
                                        <#assign p = item.prodotto>
                                        <div class="mieordini-item">
                                            <a href="${base_url}/prodotto?id=${p.idProdotto}" class="mieordini-item-media">
                                                <#-- 1. Leggiamo cosa c'è nel database (se è null, diventa vuoto "") -->
                                                <#assign imgDalDb = p.imgProdotto!"">

                                                <#-- 2. Logica del vigile urbano -->
                                                <#if imgDalDb == "">

                                                <#-- CASO A: Il prodotto non ha immagine. Scegliamo il placeholder in base al tipo -->
                                                    <#assign nomePlaceholder = "placeholder.png">
                                                    <#if p.tipo??>
                                                        <#if p.tipo == "gioco">
                                                            <#assign imgPlaceholder = "gioco_da_tavolo_img.jpg">
                                                        <#elseif p.tipo == "bustine" || p.tipo == "Bustine">
                                                            <#assign imgPlaceholder = "bustine_img.jpg">
                                                        <#elseif p.tipo == "portaDadi" || p.tipo == "Portadadi">
                                                            <#assign imgPlaceholder = "porta_dadi_img.jpg.png">
                                                        </#if>
                                                    </#if>
                                                    <#assign pathFinale = "${base_url}/public/img/prodotti/${nomePlaceholder}">

                                                <#elseif imgDalDb?contains("/")>

                                                <#-- CASO B: Prodotto NUOVO Java (contiene lo slash, es: uploads/prodotti/foto.jpg) -->
                                                    <#assign pathFinale = "${base_url}/${imgDalDb}">

                                                <#else>

                                                <#-- CASO C: Prodotto VECCHIO PHP (es: dixit.jpg) o placeholder testuale -->
                                                    <#assign pathFinale = "${base_url}/public/img/prodotti/${imgDalDb}">

                                                </#if>

                                                <#-- 3. Stampiamo il tag IMG pulitissimo -->
                                                <img src="${pathFinale}" alt="${p.nomeProdotto?html}" class="product-image">
                                            </a>

                                            <div class="mieordini-item-info">
                                                <a href="${base_url}/prodotto?id=${p.idProdotto}" class="mieordini-item-nome">
                                                    ${p.nomeProdotto?html}
                                                </a>
                                                <span class="mieordini-item-qty">Quantità: ${item.quantita}</span>
                                            </div>

                                            <div class="mieordini-item-prezzi">
                                                <#if item.scontoApplicato gt 0>
                                                    <span class="mieordini-item-prezzo-unitario-scontato">
                                                        ${(item.prezzoUnitario * (1 - item.scontoApplicato / 100))?string("0.00")} €
                                                    </span>
                                                    <span class="mieordini-item-sconto-badge">-${item.scontoApplicato?string("0")}%</span>
                                                <#else>
                                                    <span class="mieordini-item-prezzo-unitario">${item.prezzoUnitario?string("0.00")} € cad.</span>
                                                </#if>
                                                <span class="mieordini-item-totale">${item.calcolaTotaleItem()?string("0.00")} €</span>
                                            </div>
                                        </div>
                                    </#list>
                                </div>

                                <div class="mieordini-info-grid">
                                    <div class="mieordini-info-block">
                                        <h3 class="mieordini-info-title"><i class="ti ti-map-pin"></i> Indirizzo di spedizione</h3>
                                        <p class="mieordini-info-text">
                                            ${ordine.indirizzoSpedizione.via?html}<br>
                                            ${ordine.indirizzoSpedizione.cap?html} ${ordine.indirizzoSpedizione.citta?html} (${ordine.indirizzoSpedizione.provincia?html})<br>
                                            ${ordine.indirizzoSpedizione.nazione?html}
                                        </p>
                                    </div>

                                    <div class="mieordini-info-block">
                                        <h3 class="mieordini-info-title"><i class="ti ti-credit-card"></i> Metodo di pagamento</h3>
                                        <p class="mieordini-info-text">
                                            Carta terminante con <strong>${ordine.ultimeQuattroCifreCarta}</strong><br>
                                            Intestata a ${ordine.nomeTitolareCarta?html}
                                        </p>
                                    </div>
                                </div>

                            </div>

                        </div>
                    </#list>
                </div>
            <#else>
                <div class="mieordini-empty" id="mieordini-empty">
                    <div class="mieordini-empty-icon">
                        <i class="ti ti-package"></i>
                    </div>
                    <h2 class="mieordini-empty-titolo">Non hai ancora effettuato ordini</h2>
                    <p class="mieordini-empty-testo">Quando completerai un acquisto, lo troverai qui insieme allo stato della spedizione.</p>
                    <a href="${base_url}/catalogo/giochi-da-tavolo" class="mieordini-empty-btn">
                        <i class="ti ti-shopping-bag"></i> Scopri il catalogo
                    </a>
                </div>
            </#if>

        </div>
    </div>

</@layout.page>