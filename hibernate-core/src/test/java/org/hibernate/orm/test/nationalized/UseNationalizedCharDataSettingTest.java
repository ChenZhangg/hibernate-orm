/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.nationalized;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.NationalizationSupport;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.type.BasicType;
import org.hibernate.type.descriptor.java.CharacterJavaTypeDescriptor;
import org.hibernate.type.descriptor.java.StringJavaTypeDescriptor;
import org.hibernate.type.descriptor.jdbc.CharJdbcTypeDescriptor;
import org.hibernate.type.descriptor.jdbc.NCharJdbcTypeDescriptor;
import org.hibernate.type.descriptor.jdbc.NVarcharJdbcTypeDescriptor;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcTypeDescriptor;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseUnitTestCase;
import org.junit.Test;

import static org.junit.Assert.assertSame;

/**
 * Test the use of {@link AvailableSettings#USE_NATIONALIZED_CHARACTER_DATA}
 * to indicate that nationalized character data should be used.
 *
 * @author Steve Ebersole
 */
public class UseNationalizedCharDataSettingTest extends BaseUnitTestCase {
	@Test
	@TestForIssue(jiraKey = "HHH-10528")
	public void testSetting() {
		final StandardServiceRegistry ssr = new StandardServiceRegistryBuilder()
				.applySetting( AvailableSettings.USE_NATIONALIZED_CHARACTER_DATA, true )
				.build();

		try {
			final MetadataSources ms = new MetadataSources( ssr );
			ms.addAnnotatedClass( NationalizedBySettingEntity.class );

			final Metadata metadata = ms.buildMetadata();
			final PersistentClass pc = metadata.getEntityBinding( NationalizedBySettingEntity.class.getName() );
			final Property nameAttribute = pc.getProperty( "name" );
			final BasicType<?> type = (BasicType<?>) nameAttribute.getType();
			final Dialect dialect = metadata.getDatabase().getDialect();
			if ( dialect.getNationalizationSupport() != NationalizationSupport.EXPLICIT ) {
				// See issue HHH-10693
				assertSame( StringJavaTypeDescriptor.INSTANCE, type.getJavaTypeDescriptor() );
				assertSame( VarcharJdbcTypeDescriptor.INSTANCE, type.getJdbcTypeDescriptor() );
			}
			else {
				assertSame( StringJavaTypeDescriptor.INSTANCE, type.getJavaTypeDescriptor() );
				assertSame( NVarcharJdbcTypeDescriptor.INSTANCE, type.getJdbcTypeDescriptor() );
			}

		}
		finally {
			StandardServiceRegistryBuilder.destroy( ssr );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HHH-11205")
	public void testSettingOnCharType() {
		final StandardServiceRegistry ssr = new StandardServiceRegistryBuilder()
				.applySetting( AvailableSettings.USE_NATIONALIZED_CHARACTER_DATA, true )
				.build();

		try {
			final MetadataSources ms = new MetadataSources( ssr );
			ms.addAnnotatedClass( NationalizedBySettingEntity.class );

			final Metadata metadata = ms.buildMetadata();
			final PersistentClass pc = metadata.getEntityBinding( NationalizedBySettingEntity.class.getName() );
			final Property nameAttribute = pc.getProperty( "flag" );
			final BasicType<?> type = (BasicType<?>) nameAttribute.getType();
			final Dialect dialect = metadata.getDatabase().getDialect();
			if ( dialect.getNationalizationSupport() != NationalizationSupport.EXPLICIT ) {
				assertSame( CharacterJavaTypeDescriptor.INSTANCE, type.getJavaTypeDescriptor() );
				assertSame( CharJdbcTypeDescriptor.INSTANCE, type.getJdbcTypeDescriptor() );
			}
			else {
				assertSame( CharacterJavaTypeDescriptor.INSTANCE, type.getJavaTypeDescriptor() );
				assertSame( NCharJdbcTypeDescriptor.INSTANCE, type.getJdbcTypeDescriptor() );
			}

		}
		finally {
			StandardServiceRegistryBuilder.destroy( ssr );
		}
	}

	@Entity(name = "NationalizedBySettingEntity")
	@Table(name = "nationalized_by_setting_entity")
	public static class NationalizedBySettingEntity {
		@Id
		@GeneratedValue
		private long id;

		String name;
		char flag;
	}
}