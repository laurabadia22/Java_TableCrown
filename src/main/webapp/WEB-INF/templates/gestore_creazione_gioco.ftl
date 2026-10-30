<#import "common/formCreazioneProdotto.ftl" as fcp>

<@fcp.formCreazione
actionUrl="/gestore/catalogo/giochi-da-tavolo/nuovo"
urlAnnulla="/gestore/catalogo/giochi-da-tavolo"
titoloPagina="Nuovo Gioco"
currentSubpage="catalogo_giochi"
titoloForm="Aggiungi Gioco da Tavolo"
sottotitoloForm="Inserisci le regole e le specifiche del nuovo gioco da tavolo.">

    <h2 class="title is-5 form-section-title mt-6">Specifiche di Gioco</h2>
    <div class="columns is-multiline">

        <div class="column is-3">
            <div class="field">
                <label class="label">Lingua *</label>
                <div class="control">
                    <div class="select is-fullwidth">
                        <select name="lingua" required>
                            <#list lingue_enum as l>
                                <option value="${l.name()}">${l.name()?replace("_", " ")}</option>
                            </#list>
                        </select>
                    </div>
                </div>
            </div>
        </div>

        <div class="column is-3">
            <div class="field">
                <label class="label">Difficoltà *</label>
                <div class="control">
                    <div class="select is-fullwidth">
                        <select name="difficolta" required>
                            <#list difficolta_enum as d>
                                <option value="${d.name()}">${d.name()?replace("_", " ")}</option>
                            </#list>
                        </select>
                    </div>
                </div>
            </div>
        </div>

        <div class="column is-12">
            <div class="field">
                <label class="label">Categorie *</label>
                <div class="control">
                    <div class="columns is-multiline is-mobile">
                        <#list categoria_enum as cat>
                            <div class="column is-3-desktop is-half-mobile">
                                <label class="checkbox">
                                    <input type="checkbox" name="categoria" value="${cat.name()}">
                                    ${cat.name()?replace("_", " ")}
                                </label>
                            </div>
                        </#list>
                    </div>
                </div>
            </div>
        </div>

        <div class="column is-3">
            <div class="field">
                <label class="label">Giocatori Min *</label>
                <div class="control">
                    <input class="input" type="number" name="numeroGiocatoriMin" required min="1">
                </div>
            </div>
        </div>

        <div class="column is-3">
            <div class="field">
                <label class="label">Giocatori Max *</label>
                <div class="control">
                    <input class="input" type="number" name="numeroGiocatoriMax" required min="1">
                </div>
            </div>
        </div>

        <div class="column is-3">
            <div class="field">
                <label class="label">Età Minima *</label>
                <div class="control">
                    <input class="input" type="number" name="etaMinima" required min="1">
                </div>
            </div>
        </div>

        <div class="column is-3">
            <div class="field">
                <label class="label">Durata Media (min) *</label>
                <div class="control">
                    <input class="input" type="number" name="durataMedia" required min="5" step="5">
                </div>
            </div>
        </div>

        <div class="column is-6">
            <div class="field">
                <label class="label">È un'espansione?</label>
                <div class="control">
                    <div class="select is-fullwidth">
                        <select name="giocoBaseId">
                            <option value="">Nessuno (È un gioco completo)</option>
                            <#list giochiDisponibili as giocoBase>
                                <option value="${giocoBase.idProdotto}">${giocoBase.nomeProdotto}</option>
                            </#list>
                        </select>
                    </div>
                </div>
            </div>
        </div>

        <div class="column is-6">
            <div class="field">
                <label class="label">Componenti della scatola</label>
                <div id="componenti-container">
                    <!-- Primo campo di default -->
                    <div class="control is-flex mb-2" style="gap: 8px;">
                        <input class="input" type="text" name="componenti" placeholder="es. 1 tabellone">
                    </div>
                </div>

                <!-- Bottone per aggiungere nuovi campi -->
                <button type="button" id="btn-aggiungi-componente" class="button is-small is-ghost has-text-warning p-0 mt-1">
                    <i class="ti ti-plus mr-1"></i> Aggiungi un altro componente
                </button>

                <p class="help has-text-grey mt-2">Riempi quanti campi desideri (non obbligatorio).</p>
            </div>
        </div>

        <div class="column is-12 mt-2">
            <h2 class="title is-5 form-section-title mt-6">Segnalazione Danni</h2>

            <div class="notification is-warning is-light mb-4 p-4">
                <label class="checkbox mb-3 font-weight-bold">
                    <input type="checkbox" name="danneggiato" value="true">
                    Il gioco presenta un danno?
                </label>

                <div class="columns is-multiline">
                    <div class="column is-4-tablet">
                        <div class="field">
                            <label class="label is-small">Entità del Danno</label>
                            <div class="control">
                                <div class="select is-fullwidth is-small">
                                    <select name="livelloDanno">
                                        <#list danno_enum as danno>
                                            <option value="${danno.name()}">${danno.name()?replace("_", " ")}</option>
                                        </#list>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="column is-8-tablet">
                        <div class="field">
                            <label class="label is-small">Descrizione Danno (Cosa è rovinato?)</label>
                            <div class="control">
                                <input class="input is-small" type="text" name="descrizioneDanno" placeholder="es. Angolo della scatola ammaccato">
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>

    <!-- SCRIPT PER CAMPI DINAMICI -->
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const container = document.getElementById('componenti-container');
            const btnAggiungi = document.getElementById('btn-aggiungi-componente');

            if (container && btnAggiungi) {
                btnAggiungi.addEventListener('click', function() {
                    // Crea il wrapper flessibile
                    const wrapper = document.createElement('div');
                    wrapper.className = 'control is-flex mb-2';
                    wrapper.style.gap = '8px';

                    // Crea l'input di testo
                    const input = document.createElement('input');
                    input.className = 'input';
                    input.type = 'text';
                    input.name = 'componenti';
                    input.placeholder = 'es. 50 carte';

                    // Crea il bottone per rimuovere la riga
                    const btnRemove = document.createElement('button');
                    btnRemove.type = 'button';
                    // Usiamo lo stile del bottone elimina che hai già nel CSS!
                    btnRemove.className = 'button gestore-btn-elimina';
                    btnRemove.innerHTML = '<i class="ti ti-trash"></i>';
                    btnRemove.addEventListener('click', function() {
                        wrapper.remove();
                    });

                    // Assembla il tutto e lo aggiunge alla pagina
                    wrapper.appendChild(input);
                    wrapper.appendChild(btnRemove);
                    container.appendChild(wrapper);
                });
            }
        });
    </script>
</@fcp.formCreazione>