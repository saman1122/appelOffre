package fr.monappeloffre.app.web.rest;

import com.codahale.metrics.annotation.Timed;

import fr.monappeloffre.app.domain.Customer;
import fr.monappeloffre.app.domain.Project;
import fr.monappeloffre.app.domain.ProjectPic;
import fr.monappeloffre.app.domain.Provider;
import fr.monappeloffre.app.domain.Quote;
import fr.monappeloffre.app.domain.User;
import fr.monappeloffre.app.repository.ProjectRepository;
import fr.monappeloffre.app.repository.ProviderRepository;
import fr.monappeloffre.app.repository.QuoteRepository;
import fr.monappeloffre.app.repository.UserRepository;
import fr.monappeloffre.app.security.SecurityUtils;
import fr.monappeloffre.app.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Quote.
 */
@RestController
@RequestMapping("/api")
public class QuoteResource {

    private final Logger log = LoggerFactory.getLogger(QuoteResource.class);

    private static final String ENTITY_NAME = "quote";

    private final QuoteRepository quoteRepository;

    public QuoteResource(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }
    
    @Autowired
    ProjectRepository projectRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	ProviderRepository providerRepository;

    /**
     * POST  /quotes : Create a new quote.
     *
     * @param quote the quote to create
     * @return the ResponseEntity with status 201 (Created) and with body the new quote, or with status 400 (Bad Request) if the quote has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/quotes")
    @Timed
    public ResponseEntity<Quote> createQuote(@RequestBody Quote quote) throws URISyntaxException {
        log.debug("REST request to save Quote : {}", quote);
        if (quote.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new quote cannot already have an ID")).body(null);
        }
        Quote result = quoteRepository.save(quote);
        return ResponseEntity.created(new URI("/api/quotes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /quotes : Updates an existing quote.
     *
     * @param quote the quote to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated quote,
     * or with status 400 (Bad Request) if the quote is not valid,
     * or with status 500 (Internal Server Error) if the quote couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/quotes")
    @Timed
    public ResponseEntity<Quote> updateQuote(@RequestBody Quote quote) throws URISyntaxException {
        log.debug("REST request to update Quote : {}", quote);
        if (quote.getId() == null) {
            return createQuote(quote);
        }
        Quote result = quoteRepository.save(quote);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, quote.getId().toString()))
            .body(result);
    }

    /**
     * GET  /quotes : get all the quotes.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of quotes in body
     */
    @GetMapping("/quotes")
    @Timed
    public List<Quote> getAllQuotes() {
        log.debug("REST request to get all Quotes");
        return quoteRepository.findAll();
    }

    /**
     * GET  /quotes/:id : get the "id" quote.
     *
     * @param id the id of the quote to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the quote, or with status 404 (Not Found)
     */
    @GetMapping("/quotes/{id}")
    @Timed
    public ResponseEntity<Quote> getQuote(@PathVariable Long id) {
        log.debug("REST request to get Quote : {}", id);
        Quote quote = quoteRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(quote));
    }

    /**
     * DELETE  /quotes/:id : delete the "id" quote.
     *
     * @param id the id of the quote to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/quotes/{id}")
    @Timed
    public ResponseEntity<Void> deleteQuote(@PathVariable Long id) {
        log.debug("REST request to delete Quote : {}", id);
        quoteRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
    
    @PostMapping("/add-quote")
    @Timed			
	public void uploadQuote(
			@RequestParam("file") MultipartFile file, 
			@RequestParam("idProject") Long idProject
			){

		Optional<User> optional = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin());
		Long idUser = optional.isPresent() ? optional.get().getId() : 0l;

		log.debug("id User logged : "+idUser);
		Optional<Provider> optionalProvider = providerRepository.findByidUser(idUser);
		Provider provider = optionalProvider.isPresent() ? optionalProvider.get() : null;
		Project project = projectRepository.findOne(idProject);
		
		try {
			Quote quote = new Quote();
			File destinationFichier = new File(System.getProperty("user.dir")+"/src/main/webapp/content/devis/"+file.getOriginalFilename());
			log.info("Dir to save: "+destinationFichier);
			
			file.transferTo(destinationFichier);
			
			quote.setFile("content/devis/" + file.getOriginalFilename());
			quote.setProjectQU(project);
			quote.setProviderQ(provider);
			quoteRepository.save(quote);
		} catch (Exception ex) {
			log.error("Failed to upload", ex);
		}
		
	}
    
    @PostMapping("/find-quote")
    @Timed			
	public List<Quote> findQuote(@RequestParam("idProject") Long idProject){
		return quoteRepository.findByprojectQU(projectRepository.findOne(idProject));
	}
    
    @PostMapping("/find-my-quote")
    @Timed			
	public List<Quote> findQuoteOfCurrentProvider(@RequestParam("idProjects") Long[] idProjects){
    	List<Project> projects= new ArrayList<>();
		
		Optional<User> optional = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin());
		Long idUser = optional.isPresent() ? optional.get().getId() : 0l;

		log.debug("id User logged : "+idUser);
		Optional<Provider> optionalProvider = providerRepository.findByidUser(idUser);
		Provider provider = optionalProvider.isPresent() ? optionalProvider.get() : null;
		
		for (Long idProject : idProjects)
		{
			projects.add(projectRepository.findOne(idProject));
		}
		
		List<Quote> quotes = quoteRepository.findByProjectQUInAndProviderQ(projects, provider);
		
		return quotes;
	}
}
