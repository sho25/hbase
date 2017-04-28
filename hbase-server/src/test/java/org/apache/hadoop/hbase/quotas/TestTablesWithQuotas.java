begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|quotas
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
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|HRegionInfo
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
name|TableName
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
name|client
operator|.
name|Admin
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
name|client
operator|.
name|Connection
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
name|quotas
operator|.
name|QuotaObserverChore
operator|.
name|TablesWithQuotas
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
name|testclassification
operator|.
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Multimap
import|;
end_import

begin_comment
comment|/**  * Non-HBase cluster unit tests for {@link TablesWithQuotas}.  */
end_comment

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
name|TestTablesWithQuotas
block|{
specifier|private
name|Connection
name|conn
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|conn
operator|=
name|mock
argument_list|(
name|Connection
operator|.
name|class
argument_list|)
expr_stmt|;
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testImmutableGetters
parameter_list|()
block|{
name|Set
argument_list|<
name|TableName
argument_list|>
name|tablesWithTableQuotas
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|TableName
argument_list|>
name|tablesWithNamespaceQuotas
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|TablesWithQuotas
name|tables
init|=
operator|new
name|TablesWithQuotas
argument_list|(
name|conn
argument_list|,
name|conf
argument_list|)
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tn"
operator|+
name|i
argument_list|)
decl_stmt|;
name|tablesWithTableQuotas
operator|.
name|add
argument_list|(
name|tn
argument_list|)
expr_stmt|;
name|tables
operator|.
name|addTableQuotaTable
argument_list|(
name|tn
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tn_ns"
operator|+
name|i
argument_list|)
decl_stmt|;
name|tablesWithNamespaceQuotas
operator|.
name|add
argument_list|(
name|tn
argument_list|)
expr_stmt|;
name|tables
operator|.
name|addNamespaceQuotaTable
argument_list|(
name|tn
argument_list|)
expr_stmt|;
block|}
name|Set
argument_list|<
name|TableName
argument_list|>
name|actualTableQuotaTables
init|=
name|tables
operator|.
name|getTableQuotaTables
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|TableName
argument_list|>
name|actualNamespaceQuotaTables
init|=
name|tables
operator|.
name|getNamespaceQuotaTables
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|tablesWithTableQuotas
argument_list|,
name|actualTableQuotaTables
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tablesWithNamespaceQuotas
argument_list|,
name|actualNamespaceQuotaTables
argument_list|)
expr_stmt|;
try|try
block|{
name|actualTableQuotaTables
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should not be able to add an element"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
comment|// pass
block|}
try|try
block|{
name|actualNamespaceQuotaTables
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should not be able to add an element"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
comment|// pass
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInsufficientlyReportedTableFiltering
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|Integer
argument_list|>
name|reportedRegions
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|Integer
argument_list|>
name|actualRegions
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setDouble
argument_list|(
name|QuotaObserverChore
operator|.
name|QUOTA_OBSERVER_CHORE_REPORT_PERCENT_KEY
argument_list|,
literal|0.95
argument_list|)
expr_stmt|;
name|TableName
name|tooFewRegionsTable
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tn1"
argument_list|)
decl_stmt|;
name|TableName
name|sufficientRegionsTable
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tn2"
argument_list|)
decl_stmt|;
name|TableName
name|tooFewRegionsNamespaceTable
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns1"
argument_list|,
literal|"tn2"
argument_list|)
decl_stmt|;
name|TableName
name|sufficientRegionsNamespaceTable
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns1"
argument_list|,
literal|"tn2"
argument_list|)
decl_stmt|;
specifier|final
name|TablesWithQuotas
name|tables
init|=
operator|new
name|TablesWithQuotas
argument_list|(
name|conn
argument_list|,
name|conf
argument_list|)
block|{
annotation|@
name|Override
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
name|int
name|getNumRegions
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|actualRegions
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
name|int
name|getNumReportedRegions
parameter_list|(
name|TableName
name|table
parameter_list|,
name|QuotaSnapshotStore
argument_list|<
name|TableName
argument_list|>
name|tableStore
parameter_list|)
block|{
return|return
name|reportedRegions
operator|.
name|get
argument_list|(
name|table
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|tables
operator|.
name|addTableQuotaTable
argument_list|(
name|tooFewRegionsTable
argument_list|)
expr_stmt|;
name|tables
operator|.
name|addTableQuotaTable
argument_list|(
name|sufficientRegionsTable
argument_list|)
expr_stmt|;
name|tables
operator|.
name|addNamespaceQuotaTable
argument_list|(
name|tooFewRegionsNamespaceTable
argument_list|)
expr_stmt|;
name|tables
operator|.
name|addNamespaceQuotaTable
argument_list|(
name|sufficientRegionsNamespaceTable
argument_list|)
expr_stmt|;
name|reportedRegions
operator|.
name|put
argument_list|(
name|tooFewRegionsTable
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|actualRegions
operator|.
name|put
argument_list|(
name|tooFewRegionsTable
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|reportedRegions
operator|.
name|put
argument_list|(
name|sufficientRegionsTable
argument_list|,
literal|19
argument_list|)
expr_stmt|;
name|actualRegions
operator|.
name|put
argument_list|(
name|sufficientRegionsTable
argument_list|,
literal|20
argument_list|)
expr_stmt|;
name|reportedRegions
operator|.
name|put
argument_list|(
name|tooFewRegionsNamespaceTable
argument_list|,
literal|9
argument_list|)
expr_stmt|;
name|actualRegions
operator|.
name|put
argument_list|(
name|tooFewRegionsNamespaceTable
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|reportedRegions
operator|.
name|put
argument_list|(
name|sufficientRegionsNamespaceTable
argument_list|,
literal|98
argument_list|)
expr_stmt|;
name|actualRegions
operator|.
name|put
argument_list|(
name|sufficientRegionsNamespaceTable
argument_list|,
literal|100
argument_list|)
expr_stmt|;
comment|// Unused argument
name|tables
operator|.
name|filterInsufficientlyReportedTables
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|TableName
argument_list|>
name|filteredTablesWithTableQuotas
init|=
name|tables
operator|.
name|getTableQuotaTables
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|Collections
operator|.
name|singleton
argument_list|(
name|sufficientRegionsTable
argument_list|)
argument_list|,
name|filteredTablesWithTableQuotas
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|TableName
argument_list|>
name|filteredTablesWithNamespaceQutoas
init|=
name|tables
operator|.
name|getNamespaceQuotaTables
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|Collections
operator|.
name|singleton
argument_list|(
name|sufficientRegionsNamespaceTable
argument_list|)
argument_list|,
name|filteredTablesWithNamespaceQutoas
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetTablesByNamespace
parameter_list|()
block|{
specifier|final
name|TablesWithQuotas
name|tables
init|=
operator|new
name|TablesWithQuotas
argument_list|(
name|conn
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|tables
operator|.
name|addTableQuotaTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ignored1"
argument_list|)
argument_list|)
expr_stmt|;
name|tables
operator|.
name|addTableQuotaTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ignored2"
argument_list|)
argument_list|)
expr_stmt|;
name|tables
operator|.
name|addNamespaceQuotaTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns1"
argument_list|,
literal|"t1"
argument_list|)
argument_list|)
expr_stmt|;
name|tables
operator|.
name|addNamespaceQuotaTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns1"
argument_list|,
literal|"t2"
argument_list|)
argument_list|)
expr_stmt|;
name|tables
operator|.
name|addNamespaceQuotaTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns1"
argument_list|,
literal|"t3"
argument_list|)
argument_list|)
expr_stmt|;
name|tables
operator|.
name|addNamespaceQuotaTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns2"
argument_list|,
literal|"t1"
argument_list|)
argument_list|)
expr_stmt|;
name|tables
operator|.
name|addNamespaceQuotaTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns2"
argument_list|,
literal|"t2"
argument_list|)
argument_list|)
expr_stmt|;
name|Multimap
argument_list|<
name|String
argument_list|,
name|TableName
argument_list|>
name|tablesByNamespace
init|=
name|tables
operator|.
name|getTablesByNamespace
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|TableName
argument_list|>
name|tablesInNs
init|=
name|tablesByNamespace
operator|.
name|get
argument_list|(
literal|"ns1"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|tablesInNs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Unexpected results for ns1: "
operator|+
name|tablesInNs
argument_list|,
name|tablesInNs
operator|.
name|containsAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns1"
argument_list|,
literal|"t1"
argument_list|)
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns1"
argument_list|,
literal|"t2"
argument_list|)
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns1"
argument_list|,
literal|"t3"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|tablesInNs
operator|=
name|tablesByNamespace
operator|.
name|get
argument_list|(
literal|"ns2"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|tablesInNs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Unexpected results for ns2: "
operator|+
name|tablesInNs
argument_list|,
name|tablesInNs
operator|.
name|containsAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns2"
argument_list|,
literal|"t1"
argument_list|)
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns2"
argument_list|,
literal|"t2"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFilteringMissingTables
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|missingTable
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"doesNotExist"
argument_list|)
decl_stmt|;
comment|// Set up Admin to return null (match the implementation)
name|Admin
name|admin
init|=
name|mock
argument_list|(
name|Admin
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|conn
operator|.
name|getAdmin
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|admin
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|admin
operator|.
name|getTableRegions
argument_list|(
name|missingTable
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|QuotaObserverChore
name|chore
init|=
name|mock
argument_list|(
name|QuotaObserverChore
operator|.
name|class
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|Long
argument_list|>
name|regionUsage
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|TableQuotaSnapshotStore
name|store
init|=
operator|new
name|TableQuotaSnapshotStore
argument_list|(
name|conn
argument_list|,
name|chore
argument_list|,
name|regionUsage
argument_list|)
decl_stmt|;
comment|// A super dirty hack to verify that, after getting no regions for our table,
comment|// we bail out and start processing the next element (which there is none).
specifier|final
name|TablesWithQuotas
name|tables
init|=
operator|new
name|TablesWithQuotas
argument_list|(
name|conn
argument_list|,
name|conf
argument_list|)
block|{
annotation|@
name|Override
name|int
name|getNumReportedRegions
parameter_list|(
name|TableName
name|table
parameter_list|,
name|QuotaSnapshotStore
argument_list|<
name|TableName
argument_list|>
name|tableStore
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Should should not reach here"
argument_list|)
throw|;
block|}
block|}
decl_stmt|;
name|tables
operator|.
name|addTableQuotaTable
argument_list|(
name|missingTable
argument_list|)
expr_stmt|;
name|tables
operator|.
name|filterInsufficientlyReportedTables
argument_list|(
name|store
argument_list|)
expr_stmt|;
specifier|final
name|Set
argument_list|<
name|TableName
argument_list|>
name|tablesWithQuotas
init|=
name|tables
operator|.
name|getTableQuotaTables
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Expected to find no tables, but found "
operator|+
name|tablesWithQuotas
argument_list|,
name|tablesWithQuotas
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
