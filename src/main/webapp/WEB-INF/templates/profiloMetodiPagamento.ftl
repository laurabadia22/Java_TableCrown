<#import "common/layout.ftl" as layout>

<#assign extra_css>
    <link rel="stylesheet" href="${base_url}/public/css/profiloMetodiPagamento.css">
</#assign>

<#assign extra_js>
    <script>
        document.addEventListener('DOMContentLoaded', function () {

            // ── Numero carta: raggruppa le cifre a blocchi di 4 mentre si scrive ──
            var campoNumero = document.getElementById('pagamenti-form-numero');
            if (campoNumero) {
                campoNumero.addEventListener('input', function () {
                    var soloNumeri = this.value.replace(/\D/g, '').slice(0, 16);
                    this.value = soloNumeri.replace(/(.{4})/g, '$1 ').trim();
                });
            }

            // ── Scadenza: inserisce automaticamente "/" dopo il mese ──
            var campoScadenza = document.getElementById('pagamenti-form-scadenza');
            if (campoScadenza) {
                campoScadenza.addEventListener('input', function () {
                    var soloNumeri = this.value.replace(/\D/g, '').slice(0, 4);
                    if (soloNumeri.length > 2) {
                        this.value = soloNumeri.slice(0, 2) + '/' + soloNumeri.slice(2);
                    } else {
                        this.value = soloNumeri;
                    }
                });
            }

            // ── CVV: solo cifre ──
            var campoCvv = document.getElementById('pagamenti-form-cvv');
            if (campoCvv) {
                campoCvv.addEventListener('input', function () {
                    this.value = this.value.replace(/\D/g, '').slice(0, this.maxLength);
                });
            }

        });
    </script>
</#assign>

<@layout.page page_title="Metodi di Pagamento - TableCrown" extra_css=extra_css extra_js=extra_js>

    <div class="pagamenti-container">
        <div class="container">

            <div class="pagamenti-topbar">
                <a href="${base_url}/profilo" class="pagamenti-back-link">
                    <i class="ti ti-arrow-left"></i> Torna all'Area Personale
                </a>
            </div>

            <div class="pagamenti-header">
                <div class="pagamenti-header-text">
                    <span class="pagamenti-eyebrow">Area Personale</span>
                    <h1 class="pagamenti-titolo">
                        <i class="ti ti-credit-card"></i> I Miei Metodi di Pagamento
                    </h1>
                </div>

                <#if metodi?has_content>
                    <div class="pagamenti-count-badge">
                        <span class="pagamenti-count-num">${metodi?size}</span>
                        <span class="pagamenti-count-label"><#if metodi?size == 1>carta<#else>carte</#if></span>
                    </div>
                </#if>
            </div>

            <#if metodi?has_content>
                <div class="pagamenti-list">
                    <#list metodi as carta>
                        <div class="pagamenti-card <#if carta.predefinita>pagamenti-card-predefinito</#if>">">

                            <div class="pagamenti-card-top">
                                <span class="pagamenti-card-icon"><i class="ti ti-credit-card"></i></span>
                                <#if carta.predefinita>
                                    <span class="pagamenti-predefinito-badge">
                                        <i class="ti ti-star-filled"></i> Predefinita
                                    </span>
                                </#if>
                            </div>

                            <div class="pagamenti-numero-mascherato">
                                ${carta.numeroMascherato}
                            </div>

                            <div class="pagamenti-info-list">
                                <div class="pagamenti-info-row">
                                    <i class="ti ti-user"></i>
                                    <span class="pagamenti-info-value">${carta.nomeTitolare?html}</span>
                                </div>
                                <div class="pagamenti-info-row">
                                    <i class="ti ti-calendar"></i>
                                    <span class="pagamenti-info-value">Scadenza ${carta.scadenzaFormattata}</span>
                                </div>
                            </div>

                            <div class="pagamenti-actions">
                                <#if !carta.predefinita>
                                    <form action="${base_url}/profilo/pagamenti/predefinita" method="POST" class="pagamenti-action-form">
                                        <input type="hidden" name="id_carta" value="${carta.idCartaDiCredito}">
                                        <button type="submit" class="pagamenti-btn-predefinito">
                                            <i class="ti ti-star"></i> Imposta come predefinita
                                        </button>
                                    </form>
                                </#if>

                                <form action="${base_url}/profilo/pagamenti/elimina" method="POST" class="pagamenti-action-form"
                                      onsubmit="return confirm('Sei sicuro di voler eliminare questo metodo di pagamento?');">
                                    <input type="hidden" name="id_carta" value="${carta.idCartaDiCredito}">
                                    <button type="submit" class="pagamenti-btn-elimina">
                                        <i class="ti ti-trash"></i> Elimina
                                    </button>
                                </form>
                            </div>

                        </div>
                    </#list>
                </div>
            <#else>
                <div class="pagamenti-empty">
                    <div class="pagamenti-empty-icon">
                        <i class="ti ti-credit-card-off"></i>
                    </div>
                    <h2 class="pagamenti-empty-titolo">Nessun metodo di pagamento salvato</h2>
                    <p class="pagamenti-empty-testo">Aggiungi una carta per velocizzare i tuoi prossimi acquisti.</p>
                </div>
            </#if>

            <details class="pagamenti-add-details">
                <summary class="pagamenti-add-summary">
                    <i class="ti ti-plus"></i> Aggiungi nuova carta
                </summary>

                <form action="${base_url}/profilo/pagamenti/aggiungi" method="POST" class="pagamenti-form">
                    <div class="pagamenti-form-grid">
                        <div class="pagamenti-form-field pagamenti-form-field-full">
                            <label class="pagamenti-form-label" for="pagamenti-form-numero">Numero carta</label>
                            <input type="text" class="pagamenti-form-input" id="pagamenti-form-numero" name="numero_carta"
                                   inputmode="numeric" autocomplete="cc-number" placeholder="0000 0000 0000 0000" maxlength="19" required>
                        </div>

                        <div class="pagamenti-form-field pagamenti-form-field-full">
                            <label class="pagamenti-form-label" for="pagamenti-form-titolare">Titolare della carta</label>
                            <input type="text" class="pagamenti-form-input" id="pagamenti-form-titolare" name="titolare_carta"
                                   autocomplete="cc-name" required>
                        </div>

                        <div class="pagamenti-form-field">
                            <label class="pagamenti-form-label" for="pagamenti-form-scadenza">Scadenza (MM/AA)</label>
                            <input type="text" class="pagamenti-form-input" id="pagamenti-form-scadenza" name="scadenza_carta"
                                   autocomplete="cc-exp" placeholder="MM/AA" maxlength="5"
                                   pattern="(0[1-9]|1[0-2])/\d{2}" title="Formato MM/AA, es. 09/28" required>
                        </div>

                        <div class="pagamenti-form-field">
                            <label class="pagamenti-form-label" for="pagamenti-form-cvv">CVV</label>
                            <input type="text" class="pagamenti-form-input" id="pagamenti-form-cvv" name="cvv"
                                   inputmode="numeric" autocomplete="cc-csc" maxlength="4" pattern="\d{3,4}" required>
                        </div>

                        <#if metodi?has_content>
                            <div class="pagamenti-form-field pagamenti-form-field-full">
                                <label class="pagamenti-form-checkbox-label">
                                    <input type="checkbox" name="predefinita" value="true">
                                    Imposta come carta predefinita
                                </label>
                            </div>
                        </#if>

                    </div>

                    <div class="pagamenti-form-actions">
                        <button type="submit" class="pagamenti-form-btn-primary">Salva carta</button>
                    </div>
                </form>
            </details>

        </div>
    </div>

</@layout.page>