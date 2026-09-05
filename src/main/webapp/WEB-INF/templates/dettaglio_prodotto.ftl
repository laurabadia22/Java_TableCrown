<#import "common/layout.ftl" as layout>
<#import "common/prodottoCard.ftl" as pc>

<#assign cssProdotto>
    <link rel="stylesheet" href="${base_url}/public/css/prodotto.css">
</#assign>

<@layout.page
page_title="${(prodotto.nomeProdotto)!'Prodotto'} - TableCrown"
current_page="prodotto"
breadcrumbs=breadcrumbs![]
extra_css=cssProdotto>

    <div class="prodotto-container section">
        <div class="container">

            <#if messaggioSuccesso??>
                <div class="notification is-success is-light mb-5">
                    <button class="delete"></button>
                    <i class="ti ti-check"></i> ${messaggioSuccesso}
                </div>
            </#if>
            <#if messaggioErrore??>
                <div class="notification is-danger is-light mb-5">
                    <button class="delete"></button>
                    <i class="ti ti-alert-circle"></i> ${messaggioErrore}
                </div>
            </#if>

            <div class="prodotto-top columns is-desktop is-variable is-5">

                <div class="column is-5">
                    <div class="box prodotto-gallery-box">
                        <img src="${base_url}/public/img/prodotti/${(prodotto.imgProdotto)!''}"
                             alt="${(prodotto.nomeProdotto)?html}"
                             class="prodotto-gallery-img">
                    </div>
                </div>

                <div class="column is-4 is-flex is-flex-direction-column">
                    <div class="mb-2 is-flex gap-2">
                        <#if prodotto.disponibilitaProdotto??>
                            <#if prodotto.disponibilitaProdotto.name() == 'DISPONIBILE'>
                                <span class="tag is-success is-light font-weight-bold">Disponibile</span>
                            <#elseif prodotto.disponibilitaProdotto.name() == 'ESAURITO'>
                                <span class="tag is-danger is-light font-weight-bold">Esaurito</span>
                            <#elseif prodotto.disponibilitaProdotto.name() == 'NON_DISPONIBILE'>
                                <span class="tag is-dark font-weight-bold">Non disponibile</span>
                            <#else>
                                <span class="tag is-info is-light font-weight-bold">${prodotto.disponibilitaProdotto.name()}</span>
                            </#if>
                        </#if>

                        <#if livelloDanno??>
                            <span class="tag is-warning font-weight-bold">${livelloDanno.name()?replace("_", " ")}</span>
                        </#if>
                    </div>

                    <h1 class="title is-3 has-text-light mb-3">${(prodotto.nomeProdotto)?html}</h1>

                    <div class="is-flex is-align-items-center mb-4 gap-2">
                        <div class="has-text-primary">
                            <#list 1..5 as s>
                                <#if s <= valutazioneMedia!0>
                                    <i class="ti ti-star-filled"></i>
                                <#elseif (s - valutazioneMedia!0) < 1>
                                    <i class="ti ti-star-half-filled"></i>
                                <#else>
                                    <i class="ti ti-star"></i>
                                </#if>
                            </#list>
                        </div>
                        <span class="has-text-grey-light is-size-7">(${(valutazioneMedia!0)?string("0.0")} / 5.0) - ${numeroValutazioni!0} voti</span>
                    </div>

                    <div class="has-text-grey-light is-size-6 mb-5 is-flex is-flex-direction-column gap-2">
                        <#if numeroGiocatoriMin?? && numeroGiocatoriMax??>
                            <span><i class="ti ti-users mr-2"></i> ${numeroGiocatoriMin}–${numeroGiocatoriMax} giocatori</span>
                        </#if>
                        <#if etaMinima??>
                            <span><i class="ti ti-baby-carriage mr-2"></i> ${etaMinima}+ anni</span>
                        </#if>
                        <#if durataMedia??>
                            <span><i class="ti ti-clock mr-2"></i> ${durataMedia} min</span>
                        </#if>
                        <#if difficolta??>
                            <span><i class="ti ti-flame mr-2"></i> ${difficolta.name()}</span>
                        </#if>
                        <#if lingua??>
                            <span><i class="ti ti-language mr-2"></i> ${lingua.name()}</span>
                        </#if>
                        <#if categoria?? && (categoria?size > 0)>
                            <span><i class="ti ti-tags mr-2"></i> <#list categoria as cat>${cat.name()?replace("_", " ")}<#if cat_has_next>, </#if></#list></span>
                        </#if>
                    </div>

                    <div class="mt-auto mb-3">
                        <#if (prodotto.prezzoScontato < prodotto.prezzo)>
                            <span class="is-size-3 has-text-primary font-weight-bold">€${prodotto.prezzoScontato?string("0.00")}</span>
                            <span class="is-size-5 has-text-grey-light text-decoration-line-through ml-2">€${prodotto.prezzo?string("0.00")}</span>
                        <#else>
                            <span class="is-size-3 has-text-primary font-weight-bold">€${prodotto.prezzo?string("0.00")}</span>
                        </#if>
                    </div>
                </div>

                <div class="column is-3">
                    <div class="box prodotto-acquisto-box">

                        <#if prodotto.isAcquistabile()>
                            <form action="${base_url}/carrello/aggiungi" method="POST" class="mb-4">
                                <input type="hidden" name="id_prodotto" value="${prodotto.idProdotto}">

                                <div class="field mb-4">
                                    <label class="label has-text-light is-size-7">Quantità:</label>
                                    <div class="control">
                                        <input type="number" name="quantita" class="input prodotto-qty-input" value="1" min="1" max="${prodotto.quantita}">
                                    </div>
                                </div>

                                <button type="submit" class="button is-primary is-fullwidth font-weight-bold">
                                    <i class="ti ti-shopping-cart mr-2"></i> Aggiungi al Carrello
                                </button>
                            </form>
                        <#else>
                            <div class="notification is-dark has-text-centered mb-4">
                                <i class="ti ti-shopping-cart-off mb-2 is-size-4"></i><br>
                                Prodotto non acquistabile.
                            </div>
                        </#if>

                        <form action="${base_url}/wishlist/<#if isInWishlist!false>rimuovi<#else>aggiungi</#if>" method="POST">
                            <input type="hidden" name="id_prodotto" value="${prodotto.idProdotto}">
                            <button type="submit" class="button is-fullwidth is-outlined <#if isInWishlist!false>is-danger<#else>is-light</#if>">
                                <i class="ti <#if isInWishlist!false>ti-heart-filled<#else>ti-heart</#if> mr-2"></i>
                                <#if isInWishlist!false>Rimuovi dalla Wishlist<#else>Aggiungi alla Wishlist</#if>
                            </button>
                        </form>
                    </div>
                </div>
            </div>

            <hr class="prodotto-divider">

            <div class="columns">
                <div class="column is-8">
                    <h2 class="title is-4 has-text-light mb-4">Descrizione</h2>
                    <div class="content has-text-grey-light mb-6">
                        ${(prodotto.descrizioneProdotto!"Nessuna descrizione.")?replace('\n', '<br>')}
                    </div>

                    <#if descrizioneDanno??>
                        <div class="notification is-warning is-light mb-6">
                            <strong><i class="ti ti-alert-triangle mr-1"></i> Dettagli Danno:</strong><br>
                            ${(descrizioneDanno!"")?replace('\n', '<br>')}
                        </div>
                    </#if>

                    <#if componenti?? && (componenti?size > 0)>
                        <h2 class="title is-4 has-text-light mb-4">Componenti</h2>
                        <ul class="has-text-grey-light ml-4" style="list-style-type: disc;">
                            <#list componenti as comp>
                                <li class="mb-1">${comp?html}</li>
                            </#list>
                        </ul>
                    </#if>
                </div>
            </div>

            <#if correlati?? && (correlati?size > 0)>
                <hr class="prodotto-divider">
                <h2 class="title is-4 has-text-light mb-5">Forse ti può interessare...</h2>

                <div class="columns is-multiline">
                    <#list correlati as c>
                        <div class="column is-12-mobile is-4-tablet is-3-desktop flex-card-column">
                            <@pc.card p=c urlBase="${base_url}/prodotto" mostraAggiungiCarrello=true />
                        </div>
                    </#list>
                </div>
            </#if>

        </div>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', () => {
            (document.querySelectorAll('.notification .delete') || []).forEach(($delete) => {
                const $notification = $delete.parentNode;
                $delete.addEventListener('click', () => $notification.parentNode.removeChild($notification));
            });
        });
    </script>

</@layout.page>