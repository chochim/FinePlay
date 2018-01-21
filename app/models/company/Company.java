package models.company;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.ift.CellProcessor;

import common.system.MessageKeys;
import models.base.LocaleConverter;
import models.company.organization.Organization;
import models.supercsv.cellprocessor.time.FmtClientLocalDateTime;
import models.supercsv.cellprocessor.time.ParseServerLocalDateTime;
import play.i18n.MessagesApi;
import play.mvc.Controller;

@Entity
@Table(name = "COMPANIES", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) }, indexes = { @Index(columnList = "ID") })
public class Company {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final int NAME_COUNT_MAX = 2;

	@Inject
	@Transient
	private MessagesApi messages;

	@Id
	@GeneratedValue
	@Column(nullable = false)
	// Play
	private long id;

	@ElementCollection
	@MapKeyColumn(name = "LOCALE", nullable = false)
	@Convert(attributeName = "key", converter = LocaleConverter.class)
	@Column(name = "NAME", nullable = false)
	@CollectionTable(name = "COMPANY_NAMES", uniqueConstraints = { @UniqueConstraint(columnNames = { "COMPANY_ID", "LOCALE" }) }, joinColumns = @JoinColumn(nullable = false, name = "COMPANY_ID", referencedColumnName = "ID"))
	// JSR
	@Size(min = 1, message = "constraints.Size")
	private Map<Locale, String> names;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "company")
	@JoinColumns(value = { @JoinColumn(nullable = true, unique = true) })
	private Organization organization;

	@Version
	@Column(nullable = false)
	private LocalDateTime updateDateTime;

	@Transient
	private String name;

	@Transient
	private String nameJaJp;

	public static final String ID = "id";
	public static final String CODE = "code";
	public static final String CODES = "codes";
	public static final String NAME = "name";
	public static final String NAMES = "names";
	public static final String UPDATEDATETIME = "updateDateTime";

	public static final String MAXRESULT = "maxResult";
	public static final String LOCALNAME = "localName";
	public static final String UPLOADFILE = "uploadFile";
	public static final String OPERATION = "operation";

	private static final String[] HEADERS = { //
			ID, //
			UPDATEDATETIME, //
			NAME, //
			NAME + "JaJp" //
	};

	private static final CellProcessor[] READ_CELL_PROCESSORS = new CellProcessor[] { //
			new ConvertNullTo(null, new ParseLong()), //
			new ParseServerLocalDateTime(), //
			null, //
			null //
	};

	private static final CellProcessor[] WRITE_CELL_PROCESSORS = new CellProcessor[] { //
			null, //
			new FmtClientLocalDateTime(), //
			null, //
			null //
	};

	public static String[] getHeaders() {

		return HEADERS;
	}

	public static CellProcessor[] getReadCellProcessors() {

		return READ_CELL_PROCESSORS;
	}

	public static CellProcessor[] getWriteCellProcessors() {

		return WRITE_CELL_PROCESSORS;
	}

	public void beforeWrite() {

		int i = 0;
		for (final Entry<Locale, String> localeToName : getNames().entrySet()) {

			final Locale locale = localeToName.getKey();
			final String name = localeToName.getValue();
			if (Locale.US == locale) {

				setName(name);
			} else if (Locale.JAPAN == locale) {

				setNameJaJp(name);
			} else {

				throw new IllegalStateException(messages.get(Controller.ctx().lang(), MessageKeys.JAVA_ERROR_SIZE, 0, NAME_COUNT_MAX) + " :" + i);
			}

			i++;
		}
	}

	public void afterRead() {

		final Map<Locale, String> names = new HashMap<>();
		names.put(Locale.US, getName());
		names.put(Locale.JAPAN, getNameJaJp());

		if (!(1 <= names.size())) {

			throw new IllegalStateException(messages.get(Controller.ctx().lang(), MessageKeys.JAVA_ERROR_SIZE, 0, NAME_COUNT_MAX) + " :" + names.size());
		}

		setNames(names);
	}

	public long getId() {

		return id;
	}

	public void setId(long id) {

		this.id = id;
	}

	public Map<Locale, String> getNames() {

		return names;
	}

	public void setNames(Map<Locale, String> names) {

		this.names = names;
	}

	public Organization getOrganization() {

		return organization;
	}

	public void setOrganization(Organization organization) {

		this.organization = organization;
	}

	public LocalDateTime getUpdateDateTime() {

		return updateDateTime;
	}

	public void setUpdateDateTime(LocalDateTime updateDateTime) {

		this.updateDateTime = updateDateTime;
	}

	public String getName() {

		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

	public String getNameJaJp() {

		return nameJaJp;
	}

	public void setNameJaJp(String nameJaJp) {

		this.nameJaJp = nameJaJp;
	}
}
