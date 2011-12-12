begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|metrics
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseConfiguration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|HRegion
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|ClassSize
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jettison
operator|.
name|json
operator|.
name|JSONException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jettison
operator|.
name|json
operator|.
name|JSONStringer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestSchemaConfigured
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestSchemaConfigured
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"myTable"
decl_stmt|;
specifier|private
specifier|final
name|String
name|CF_NAME
init|=
literal|"myColumnFamily"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Path
name|TMP_HFILE_PATH
init|=
operator|new
name|Path
argument_list|(
literal|"/hbase/myTable/myRegion/"
operator|+
name|HRegion
operator|.
name|REGION_TEMP_SUBDIR
operator|+
literal|"/hfilename"
argument_list|)
decl_stmt|;
comment|/** Test if toString generates real JSON */
annotation|@
name|Test
specifier|public
name|void
name|testToString
parameter_list|()
throws|throws
name|JSONException
block|{
name|SchemaConfigured
name|sc
init|=
operator|new
name|SchemaConfigured
argument_list|(
literal|null
argument_list|,
name|TABLE_NAME
argument_list|,
name|CF_NAME
argument_list|)
decl_stmt|;
name|JSONStringer
name|json
init|=
operator|new
name|JSONStringer
argument_list|()
decl_stmt|;
name|json
operator|.
name|object
argument_list|()
expr_stmt|;
name|json
operator|.
name|key
argument_list|(
literal|"tableName"
argument_list|)
expr_stmt|;
name|json
operator|.
name|value
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|json
operator|.
name|key
argument_list|(
literal|"cfName"
argument_list|)
expr_stmt|;
name|json
operator|.
name|value
argument_list|(
name|CF_NAME
argument_list|)
expr_stmt|;
name|json
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|json
operator|.
name|toString
argument_list|()
argument_list|,
name|sc
operator|.
name|schemaConfAsJSON
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** Don't allow requesting metrics before setting table/CF name */
annotation|@
name|Test
specifier|public
name|void
name|testDelayedInitialization
parameter_list|()
block|{
name|SchemaConfigured
name|unconfigured
init|=
operator|new
name|SchemaConfigured
argument_list|()
decl_stmt|;
try|try
block|{
name|unconfigured
operator|.
name|getSchemaMetrics
argument_list|()
expr_stmt|;
name|fail
argument_list|(
name|IllegalStateException
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|ex
parameter_list|)
block|{
name|assertTrue
argument_list|(
literal|"Unexpected exception message: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|Pattern
operator|.
name|matches
argument_list|(
literal|".* metrics requested before .* initialization.*"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Expected exception: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|SchemaMetrics
operator|.
name|setUseTableNameInTest
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|SchemaConfigured
name|other
init|=
operator|new
name|SchemaConfigured
argument_list|(
literal|null
argument_list|,
name|TABLE_NAME
argument_list|,
name|CF_NAME
argument_list|)
decl_stmt|;
name|other
operator|.
name|passSchemaMetricsTo
argument_list|(
name|unconfigured
argument_list|)
expr_stmt|;
name|unconfigured
operator|.
name|getSchemaMetrics
argument_list|()
expr_stmt|;
comment|// now this should succeed
block|}
comment|/** Don't allow setting table/CF name twice */
annotation|@
name|Test
specifier|public
name|void
name|testInitializingTwice
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|4
condition|;
operator|++
name|i
control|)
block|{
name|SchemaConfigured
name|sc
init|=
operator|new
name|SchemaConfigured
argument_list|(
name|conf
argument_list|,
name|TABLE_NAME
argument_list|,
name|CF_NAME
argument_list|)
decl_stmt|;
name|SchemaConfigured
name|target
init|=
operator|new
name|SchemaConfigured
argument_list|(
name|conf
argument_list|,
name|TABLE_NAME
operator|+
operator|(
name|i
operator|%
literal|2
operator|==
literal|1
condition|?
literal|"1"
else|:
literal|""
operator|)
argument_list|,
name|CF_NAME
operator|+
operator|(
operator|(
name|i
operator|&
literal|2
operator|)
operator|!=
literal|0
condition|?
literal|"1"
else|:
literal|""
operator|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|sc
operator|.
name|passSchemaMetricsTo
argument_list|(
name|target
argument_list|)
expr_stmt|;
comment|// No exception expected.
continue|continue;
block|}
name|String
name|testDesc
init|=
literal|"Trying to re-configure "
operator|+
name|target
operator|.
name|schemaConfAsJSON
argument_list|()
operator|+
literal|" with "
operator|+
name|sc
operator|.
name|schemaConfAsJSON
argument_list|()
decl_stmt|;
try|try
block|{
name|sc
operator|.
name|passSchemaMetricsTo
argument_list|(
name|target
argument_list|)
expr_stmt|;
name|fail
argument_list|(
name|IllegalArgumentException
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
specifier|final
name|String
name|errorMsg
init|=
name|testDesc
operator|+
literal|". Unexpected exception message: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
decl_stmt|;
specifier|final
name|String
name|exceptionRegex
init|=
literal|"Trying to change table .* CF .*"
decl_stmt|;
name|assertTrue
argument_list|(
name|errorMsg
argument_list|,
name|Pattern
operator|.
name|matches
argument_list|(
name|exceptionRegex
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Expected exception: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalStateException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testConfigureWithUnconfigured
parameter_list|()
block|{
name|SchemaConfigured
name|unconfigured
init|=
operator|new
name|SchemaConfigured
argument_list|()
decl_stmt|;
name|SchemaConfigured
name|target
init|=
operator|new
name|SchemaConfigured
argument_list|()
decl_stmt|;
name|unconfigured
operator|.
name|passSchemaMetricsTo
argument_list|(
name|target
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testConfigurePartiallyDefined
parameter_list|()
block|{
specifier|final
name|SchemaConfigured
name|sc
init|=
operator|new
name|SchemaConfigured
argument_list|(
literal|null
argument_list|,
literal|"t1"
argument_list|,
literal|"cf1"
argument_list|)
decl_stmt|;
specifier|final
name|SchemaConfigured
name|target1
init|=
operator|new
name|SchemaConfigured
argument_list|(
literal|null
argument_list|,
literal|"t2"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|sc
operator|.
name|passSchemaMetricsTo
argument_list|(
name|target1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"t2"
argument_list|,
name|target1
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"cf1"
argument_list|,
name|target1
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|SchemaConfigured
name|target2
init|=
operator|new
name|SchemaConfigured
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|"cf2"
argument_list|)
decl_stmt|;
name|sc
operator|.
name|passSchemaMetricsTo
argument_list|(
name|target2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"t1"
argument_list|,
name|target2
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"cf2"
argument_list|,
name|target2
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|SchemaConfigured
name|target3
init|=
operator|new
name|SchemaConfigured
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|sc
operator|.
name|passSchemaMetricsTo
argument_list|(
name|target3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"t1"
argument_list|,
name|target2
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"cf1"
argument_list|,
name|target2
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testConflictingConf
parameter_list|()
block|{
name|SchemaConfigured
name|sc
init|=
operator|new
name|SchemaConfigured
argument_list|(
literal|null
argument_list|,
literal|"t1"
argument_list|,
literal|"cf1"
argument_list|)
decl_stmt|;
name|SchemaConfigured
name|target
init|=
operator|new
name|SchemaConfigured
argument_list|(
literal|null
argument_list|,
literal|"t2"
argument_list|,
literal|"cf1"
argument_list|)
decl_stmt|;
name|target
operator|.
name|passSchemaMetricsTo
argument_list|(
name|sc
argument_list|)
expr_stmt|;
block|}
comment|/**    * When the "column family" deduced from the path is ".tmp" (this happens    * for files written on compaction) we allow re-setting the CF to another    * value.    */
annotation|@
name|Test
specifier|public
name|void
name|testTmpPath
parameter_list|()
block|{
name|SchemaConfigured
name|sc
init|=
operator|new
name|SchemaConfigured
argument_list|(
literal|null
argument_list|,
literal|"myTable"
argument_list|,
literal|"myCF"
argument_list|)
decl_stmt|;
name|SchemaConfigured
name|target
init|=
operator|new
name|SchemaConfigured
argument_list|(
name|TMP_HFILE_PATH
argument_list|)
decl_stmt|;
name|sc
operator|.
name|passSchemaMetricsTo
argument_list|(
name|target
argument_list|)
expr_stmt|;
block|}
comment|/**    * Even if CF is initially undefined (".tmp"), we don't allow to change    * table name.    */
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testTmpPathButInvalidTable
parameter_list|()
block|{
name|SchemaConfigured
name|sc
init|=
operator|new
name|SchemaConfigured
argument_list|(
literal|null
argument_list|,
literal|"anotherTable"
argument_list|,
literal|"myCF"
argument_list|)
decl_stmt|;
name|SchemaConfigured
name|target
init|=
operator|new
name|SchemaConfigured
argument_list|(
name|TMP_HFILE_PATH
argument_list|)
decl_stmt|;
name|sc
operator|.
name|passSchemaMetricsTo
argument_list|(
name|target
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSchemaConfigurationHook
parameter_list|()
block|{
name|SchemaConfigured
name|sc
init|=
operator|new
name|SchemaConfigured
argument_list|(
literal|null
argument_list|,
literal|"myTable"
argument_list|,
literal|"myCF"
argument_list|)
decl_stmt|;
specifier|final
name|StringBuilder
name|newCF
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
specifier|final
name|StringBuilder
name|newTable
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|SchemaConfigured
name|target
init|=
operator|new
name|SchemaConfigured
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|void
name|schemaConfigurationChanged
parameter_list|()
block|{
name|newCF
operator|.
name|append
argument_list|(
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
name|newTable
operator|.
name|append
argument_list|(
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|sc
operator|.
name|passSchemaMetricsTo
argument_list|(
name|target
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"myTable"
argument_list|,
name|newTable
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"myCF"
argument_list|,
name|newCF
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testResetSchemaMetricsConf
parameter_list|()
block|{
name|SchemaConfigured
name|target
init|=
operator|new
name|SchemaConfigured
argument_list|(
literal|null
argument_list|,
literal|"t1"
argument_list|,
literal|"cf1"
argument_list|)
decl_stmt|;
name|SchemaConfigured
operator|.
name|resetSchemaMetricsConf
argument_list|(
name|target
argument_list|)
expr_stmt|;
operator|new
name|SchemaConfigured
argument_list|(
literal|null
argument_list|,
literal|"t2"
argument_list|,
literal|"cf2"
argument_list|)
operator|.
name|passSchemaMetricsTo
argument_list|(
name|target
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"t2"
argument_list|,
name|target
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"cf2"
argument_list|,
name|target
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPathTooShort
parameter_list|()
block|{
comment|// This has too few path components (four, the first one is empty).
name|SchemaConfigured
name|sc1
init|=
operator|new
name|SchemaConfigured
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/a/b/c/d"
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|SchemaMetrics
operator|.
name|UNKNOWN
argument_list|,
name|sc1
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|SchemaMetrics
operator|.
name|UNKNOWN
argument_list|,
name|sc1
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
name|SchemaConfigured
name|sc2
init|=
operator|new
name|SchemaConfigured
argument_list|(
operator|new
name|Path
argument_list|(
literal|"a/b/c/d"
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|SchemaMetrics
operator|.
name|UNKNOWN
argument_list|,
name|sc2
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|SchemaMetrics
operator|.
name|UNKNOWN
argument_list|,
name|sc2
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
name|SchemaConfigured
name|sc3
init|=
operator|new
name|SchemaConfigured
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/hbase/tableName/regionId/cfName/hfileName"
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"tableName"
argument_list|,
name|sc3
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"cfName"
argument_list|,
name|sc3
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
name|SchemaConfigured
name|sc4
init|=
operator|new
name|SchemaConfigured
argument_list|(
operator|new
name|Path
argument_list|(
literal|"hbase/tableName/regionId/cfName/hfileName"
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"tableName"
argument_list|,
name|sc4
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"cfName"
argument_list|,
name|sc4
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

