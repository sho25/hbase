begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
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
name|assertFalse
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
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|hbase
operator|.
name|HColumnDescriptor
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
name|HConstants
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
name|HTableDescriptor
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
name|LargeTests
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
name|Bytes
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
name|zookeeper
operator|.
name|MasterSchemaChangeTracker
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
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
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestInstantSchemaChange
extends|extends
name|InstantSchemaChangeTestBase
block|{
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testInstantSchemaChangeForModifyTable
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
name|String
name|tableName
init|=
literal|"testInstantSchemaChangeForModifyTable"
decl_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Start testInstantSchemaChangeForModifyTable()"
argument_list|)
expr_stmt|;
name|HTable
name|ht
init|=
name|createTableAndValidate
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|String
name|newFamily
init|=
literal|"newFamily"
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|newFamily
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|htd
argument_list|)
expr_stmt|;
name|waitForSchemaChangeProcess
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|msct
operator|.
name|doesSchemaChangeNodeExists
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|Put
name|put1
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put1
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newFamily
argument_list|)
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|ht
operator|.
name|put
argument_list|(
name|put1
argument_list|)
expr_stmt|;
name|Get
name|get1
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get1
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newFamily
argument_list|)
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|ht
operator|.
name|get
argument_list|(
name|get1
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tvalue
init|=
name|r
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newFamily
argument_list|)
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
name|int
name|result
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|value
argument_list|,
name|tvalue
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|result
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"END testInstantSchemaChangeForModifyTable()"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInstantSchemaChangeForAddColumn
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Start testInstantSchemaChangeForAddColumn() "
argument_list|)
expr_stmt|;
name|String
name|tableName
init|=
literal|"testSchemachangeForAddColumn"
decl_stmt|;
name|HTable
name|ht
init|=
name|createTableAndValidate
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|String
name|newFamily
init|=
literal|"newFamily"
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"newFamily"
argument_list|)
decl_stmt|;
name|admin
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
name|waitForSchemaChangeProcess
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|msct
operator|.
name|doesSchemaChangeNodeExists
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|Put
name|put1
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put1
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newFamily
argument_list|)
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"******** Put into new column family "
argument_list|)
expr_stmt|;
name|ht
operator|.
name|put
argument_list|(
name|put1
argument_list|)
expr_stmt|;
name|Get
name|get1
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get1
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newFamily
argument_list|)
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|ht
operator|.
name|get
argument_list|(
name|get1
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tvalue
init|=
name|r
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newFamily
argument_list|)
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|" Value put = "
operator|+
name|value
operator|+
literal|" value from table = "
operator|+
name|tvalue
argument_list|)
expr_stmt|;
name|int
name|result
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|value
argument_list|,
name|tvalue
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|result
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"End testInstantSchemaChangeForAddColumn() "
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInstantSchemaChangeForModifyColumn
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Start testInstantSchemaChangeForModifyColumn() "
argument_list|)
expr_stmt|;
name|String
name|tableName
init|=
literal|"testSchemachangeForModifyColumn"
decl_stmt|;
name|createTableAndValidate
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|99
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setBlockCacheEnabled
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
name|waitForSchemaChangeProcess
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|msct
operator|.
name|doesSchemaChangeNodeExists
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegion
argument_list|>
name|onlineRegions
init|=
name|miniHBaseCluster
operator|.
name|getRegions
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testSchemachangeForModifyColumn"
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegion
name|onlineRegion
range|:
name|onlineRegions
control|)
block|{
name|HTableDescriptor
name|htd
init|=
name|onlineRegion
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
name|HColumnDescriptor
name|tableHcd
init|=
name|htd
operator|.
name|getFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|tableHcd
operator|.
name|isBlockCacheEnabled
argument_list|()
operator|==
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableHcd
operator|.
name|getMaxVersions
argument_list|()
argument_list|,
literal|99
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"End testInstantSchemaChangeForModifyColumn() "
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInstantSchemaChangeForDeleteColumn
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Start testInstantSchemaChangeForDeleteColumn() "
argument_list|)
expr_stmt|;
name|String
name|tableName
init|=
literal|"testSchemachangeForDeleteColumn"
decl_stmt|;
name|int
name|numTables
init|=
literal|0
decl_stmt|;
name|HTableDescriptor
index|[]
name|tables
init|=
name|admin
operator|.
name|listTables
argument_list|()
decl_stmt|;
if|if
condition|(
name|tables
operator|!=
literal|null
condition|)
block|{
name|numTables
operator|=
name|tables
operator|.
name|length
expr_stmt|;
block|}
name|byte
index|[]
index|[]
name|FAMILIES
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"B"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C"
argument_list|)
block|}
decl_stmt|;
name|HTable
name|ht
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|FAMILIES
argument_list|)
decl_stmt|;
name|tables
operator|=
name|this
operator|.
name|admin
operator|.
name|listTables
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|numTables
operator|+
literal|1
argument_list|,
name|tables
operator|.
name|length
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Table testSchemachangeForDeleteColumn created"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteColumn
argument_list|(
name|tableName
argument_list|,
literal|"C"
argument_list|)
expr_stmt|;
name|waitForSchemaChangeProcess
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|msct
operator|.
name|doesSchemaChangeNodeExists
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|modifiedHtd
init|=
name|this
operator|.
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
name|modifiedHtd
operator|.
name|getFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C"
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|hcd
operator|==
literal|null
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"End testInstantSchemaChangeForDeleteColumn() "
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInstantSchemaChangeWhenTableIsNotEnabled
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
block|{
specifier|final
name|String
name|tableName
init|=
literal|"testInstantSchemaChangeWhenTableIsDisabled"
decl_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Start testInstantSchemaChangeWhenTableIsDisabled()"
argument_list|)
expr_stmt|;
name|HTable
name|ht
init|=
name|createTableAndValidate
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// Disable table
name|admin
operator|.
name|disableTable
argument_list|(
literal|"testInstantSchemaChangeWhenTableIsDisabled"
argument_list|)
expr_stmt|;
comment|// perform schema changes
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"newFamily"
argument_list|)
decl_stmt|;
name|admin
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
name|MasterSchemaChangeTracker
name|msct
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getSchemaChangeTracker
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|msct
operator|.
name|doesSchemaChangeNodeExists
argument_list|(
name|tableName
argument_list|)
operator|==
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that when concurrent alter requests are received for a table we don't miss any.    * @throws IOException    * @throws KeeperException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testConcurrentInstantSchemaChangeForAddColumn
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
specifier|final
name|String
name|tableName
init|=
literal|"testConcurrentInstantSchemaChangeForModifyTable"
decl_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Start testConcurrentInstantSchemaChangeForModifyTable()"
argument_list|)
expr_stmt|;
name|HTable
name|ht
init|=
name|createTableAndValidate
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Runnable
name|run1
init|=
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"family1"
argument_list|)
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|ioe
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|Runnable
name|run2
init|=
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"family2"
argument_list|)
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|ioe
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|run1
operator|.
name|run
argument_list|()
expr_stmt|;
comment|// We have to add a sleep here as in concurrent scenarios the HTD update
comment|// in HDFS fails and returns with null HTD. This needs to be investigated,
comment|// but it doesn't impact the instant alter functionality in any way.
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|run2
operator|.
name|run
argument_list|()
expr_stmt|;
name|waitForSchemaChangeProcess
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|Put
name|put1
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put1
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family1"
argument_list|)
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|ht
operator|.
name|put
argument_list|(
name|put1
argument_list|)
expr_stmt|;
name|Get
name|get1
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get1
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family1"
argument_list|)
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|ht
operator|.
name|get
argument_list|(
name|get1
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tvalue
init|=
name|r
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family1"
argument_list|)
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
name|int
name|result
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|value
argument_list|,
name|tvalue
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|result
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
name|Put
name|put2
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put2
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family2"
argument_list|)
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|ht
operator|.
name|put
argument_list|(
name|put2
argument_list|)
expr_stmt|;
name|Get
name|get2
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get2
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family2"
argument_list|)
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
name|Result
name|r2
init|=
name|ht
operator|.
name|get
argument_list|(
name|get2
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tvalue2
init|=
name|r2
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family2"
argument_list|)
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
name|int
name|result2
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|value
argument_list|,
name|tvalue2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|result2
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"END testConcurrentInstantSchemaChangeForModifyTable()"
argument_list|)
expr_stmt|;
block|}
comment|/**    * The schema change request blocks while a LB run is in progress. This    * test validates this behavior.    * @throws IOException    * @throws InterruptedException    * @throws KeeperException    */
annotation|@
name|Test
specifier|public
name|void
name|testConcurrentInstantSchemaChangeAndLoadBalancerRun
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|KeeperException
block|{
specifier|final
name|String
name|tableName
init|=
literal|"testInstantSchemaChangeWithLoadBalancerRunning"
decl_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Start testInstantSchemaChangeWithLoadBalancerRunning()"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|newFamily
init|=
literal|"newFamily"
decl_stmt|;
name|HTable
name|ht
init|=
name|createTableAndValidate
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
specifier|final
name|MasterSchemaChangeTracker
name|msct
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getSchemaChangeTracker
argument_list|()
decl_stmt|;
name|Runnable
name|balancer
init|=
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
comment|// run the balancer now.
name|miniHBaseCluster
operator|.
name|getMaster
argument_list|()
operator|.
name|balance
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|Runnable
name|schemaChanger
init|=
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|newFamily
argument_list|)
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|ioe
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|balancer
operator|.
name|run
argument_list|()
expr_stmt|;
name|schemaChanger
operator|.
name|run
argument_list|()
expr_stmt|;
name|waitForSchemaChangeProcess
argument_list|(
name|tableName
argument_list|,
literal|40000
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|msct
operator|.
name|doesSchemaChangeNodeExists
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|Put
name|put1
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put1
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newFamily
argument_list|)
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"******** Put into new column family "
argument_list|)
expr_stmt|;
name|ht
operator|.
name|put
argument_list|(
name|put1
argument_list|)
expr_stmt|;
name|ht
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"******** Get from new column family "
argument_list|)
expr_stmt|;
name|Get
name|get1
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get1
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newFamily
argument_list|)
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|ht
operator|.
name|get
argument_list|(
name|get1
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tvalue
init|=
name|r
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newFamily
argument_list|)
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|" Value put = "
operator|+
name|value
operator|+
literal|" value from table = "
operator|+
name|tvalue
argument_list|)
expr_stmt|;
name|int
name|result
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|value
argument_list|,
name|tvalue
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|result
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"End testInstantSchemaChangeWithLoadBalancerRunning() "
argument_list|)
expr_stmt|;
block|}
comment|/**    * This test validates two things. One is that the LoadBalancer does not run when a schema    * change process is in progress. The second thing is that it also checks that failed/expired    * schema changes are expired to unblock the load balancer run.    *    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|70000
argument_list|)
specifier|public
name|void
name|testLoadBalancerBlocksDuringSchemaChangeRequests
parameter_list|()
throws|throws
name|KeeperException
throws|,
name|IOException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Start testConcurrentLoadBalancerSchemaChangeRequests() "
argument_list|)
expr_stmt|;
specifier|final
name|MasterSchemaChangeTracker
name|msct
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getSchemaChangeTracker
argument_list|()
decl_stmt|;
comment|// Test that the load balancer does not run while an in-flight schema
comment|// change operation is in progress.
comment|// Simulate a new schema change request.
name|msct
operator|.
name|createSchemaChangeNode
argument_list|(
literal|"testLoadBalancerBlocks"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// The schema change node is created.
name|assertTrue
argument_list|(
name|msct
operator|.
name|doesSchemaChangeNodeExists
argument_list|(
literal|"testLoadBalancerBlocks"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now, request an explicit LB run.
name|Runnable
name|balancer1
init|=
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
comment|// run the balancer now.
name|miniHBaseCluster
operator|.
name|getMaster
argument_list|()
operator|.
name|balance
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|balancer1
operator|.
name|run
argument_list|()
expr_stmt|;
comment|// Load balancer should not run now.
name|assertTrue
argument_list|(
name|miniHBaseCluster
operator|.
name|getMaster
argument_list|()
operator|.
name|isLoadBalancerRunning
argument_list|()
operator|==
literal|false
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"testConcurrentLoadBalancerSchemaChangeRequests Asserted"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"End testConcurrentLoadBalancerSchemaChangeRequests() "
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that instant schema change blocks while LB is running.    * @throws KeeperException    * @throws IOException    * @throws InterruptedException    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|10000
argument_list|)
specifier|public
name|void
name|testInstantSchemaChangeBlocksDuringLoadBalancerRun
parameter_list|()
throws|throws
name|KeeperException
throws|,
name|IOException
throws|,
name|InterruptedException
block|{
specifier|final
name|MasterSchemaChangeTracker
name|msct
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getSchemaChangeTracker
argument_list|()
decl_stmt|;
specifier|final
name|String
name|tableName
init|=
literal|"testInstantSchemaChangeBlocksDuringLoadBalancerRun"
decl_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Start testInstantSchemaChangeBlocksDuringLoadBalancerRun()"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|newFamily
init|=
literal|"newFamily"
decl_stmt|;
name|createTableAndValidate
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// Test that the schema change request does not run while an in-flight LB run
comment|// is in progress.
comment|// First, request an explicit LB run.
name|Runnable
name|balancer1
init|=
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
comment|// run the balancer now.
name|miniHBaseCluster
operator|.
name|getMaster
argument_list|()
operator|.
name|balance
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|Runnable
name|schemaChanger
init|=
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|newFamily
argument_list|)
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|ioe
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|Thread
name|t1
init|=
operator|new
name|Thread
argument_list|(
name|balancer1
argument_list|)
decl_stmt|;
name|Thread
name|t2
init|=
operator|new
name|Thread
argument_list|(
name|schemaChanger
argument_list|)
decl_stmt|;
name|t1
operator|.
name|start
argument_list|()
expr_stmt|;
name|t2
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// check that they both happen concurrently
name|Runnable
name|balancerCheck
init|=
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
comment|// check whether balancer is running.
while|while
condition|(
operator|!
name|miniHBaseCluster
operator|.
name|getMaster
argument_list|()
operator|.
name|isLoadBalancerRunning
argument_list|()
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
try|try
block|{
name|assertFalse
argument_list|(
name|msct
operator|.
name|doesSchemaChangeNodeExists
argument_list|(
literal|"testSchemaChangeBlocks"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|ke
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Load Balancer is now running or skipped"
argument_list|)
expr_stmt|;
while|while
condition|(
name|miniHBaseCluster
operator|.
name|getMaster
argument_list|()
operator|.
name|isLoadBalancerRunning
argument_list|()
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|miniHBaseCluster
operator|.
name|getMaster
argument_list|()
operator|.
name|isLoadBalancerRunning
argument_list|()
operator|==
literal|false
argument_list|)
expr_stmt|;
try|try
block|{
name|assertTrue
argument_list|(
name|msct
operator|.
name|doesSchemaChangeNodeExists
argument_list|(
literal|"testSchemaChangeBlocks"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{          }
block|}
block|}
decl_stmt|;
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
name|balancerCheck
argument_list|)
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|t
operator|.
name|join
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
comment|// Load balancer should not run now.
comment|//assertTrue(miniHBaseCluster.getMaster().isLoadBalancerRunning() == false);
comment|// Schema change request node should now exist.
comment|// assertTrue(msct.doesSchemaChangeNodeExists("testSchemaChangeBlocks"));
name|LOG
operator|.
name|debug
argument_list|(
literal|"testInstantSchemaChangeBlocksDuringLoadBalancerRun Asserted"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"End testInstantSchemaChangeBlocksDuringLoadBalancerRun() "
argument_list|)
expr_stmt|;
block|}
comment|/**    * To test the schema janitor (that it cleans expired/failed schema alter attempts) we    * simply create a fake table (that doesn't exist, with fake number of online regions) in ZK.    * This schema alter request will time out (after 30 seconds) and our janitor will clean it up.    * regions    * @throws IOException    * @throws KeeperException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testInstantSchemaJanitor
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"testInstantSchemaWithFailedExpiredOperations() "
argument_list|)
expr_stmt|;
name|String
name|fakeTableName
init|=
literal|"testInstantSchemaWithFailedExpiredOperations"
decl_stmt|;
name|MasterSchemaChangeTracker
name|msct
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getSchemaChangeTracker
argument_list|()
decl_stmt|;
name|msct
operator|.
name|createSchemaChangeNode
argument_list|(
name|fakeTableName
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|msct
operator|.
name|getSchemaChangeNodePathForTable
argument_list|(
name|fakeTableName
argument_list|)
operator|+
literal|" created"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|40000
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|msct
operator|.
name|doesSchemaChangeNodeExists
argument_list|(
name|fakeTableName
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|msct
operator|.
name|getSchemaChangeNodePathForTable
argument_list|(
name|fakeTableName
argument_list|)
operator|+
literal|" deleted"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"END testInstantSchemaWithFailedExpiredOperations() "
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

