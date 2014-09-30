begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|thrift2
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|*
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
name|HBaseAdmin
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
name|HTable
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
name|HTableInterface
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
name|Table
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
name|ClientTests
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
name|MediumTests
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
name|util
operator|.
name|PoolMap
operator|.
name|PoolType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|*
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
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Suite
import|;
end_import

begin_comment
comment|/**  * Tests HTablePool.  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|Suite
operator|.
name|class
argument_list|)
annotation|@
name|Suite
operator|.
name|SuiteClasses
argument_list|(
block|{
name|TestHTablePool
operator|.
name|TestHTableReusablePool
operator|.
name|class
block|,
name|TestHTablePool
operator|.
name|TestHTableThreadLocalPool
operator|.
name|class
block|}
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|ClientTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestHTablePool
block|{
specifier|private
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|TABLENAME
init|=
literal|"TestHTablePool"
decl_stmt|;
specifier|public
specifier|abstract
specifier|static
class|class
name|TestHTablePoolType
block|{
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLENAME
argument_list|)
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|PoolType
name|getPoolType
parameter_list|()
function_decl|;
annotation|@
name|Test
specifier|public
name|void
name|testTableWithStringName
parameter_list|()
throws|throws
name|Exception
block|{
name|HTablePool
name|pool
init|=
operator|new
name|HTablePool
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|getPoolType
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|tableName
init|=
name|TABLENAME
decl_stmt|;
comment|// Request a table from an empty pool
name|Table
name|table
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|table
argument_list|)
expr_stmt|;
comment|// Close table (returns table to the pool)
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Request a table of the same name
name|Table
name|sameTable
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|table
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|,
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|sameTable
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableWithByteArrayName
parameter_list|()
throws|throws
name|IOException
block|{
name|HTablePool
name|pool
init|=
operator|new
name|HTablePool
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|getPoolType
argument_list|()
argument_list|)
decl_stmt|;
comment|// Request a table from an empty pool
name|Table
name|table
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|table
argument_list|)
expr_stmt|;
comment|// Close table (returns table to the pool)
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Request a table of the same name
name|Table
name|sameTable
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|table
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|,
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|sameTable
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTablesWithDifferentNames
parameter_list|()
throws|throws
name|IOException
block|{
name|HTablePool
name|pool
init|=
operator|new
name|HTablePool
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|getPoolType
argument_list|()
argument_list|)
decl_stmt|;
comment|// We add the class to the table name as the HBase cluster is reused
comment|//  during the tests: this gives naming unicity.
name|byte
index|[]
name|otherTable
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"OtherTable_"
operator|+
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|otherTable
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
comment|// Request a table from an empty pool
name|Table
name|table1
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|Table
name|table2
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|otherTable
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|table2
argument_list|)
expr_stmt|;
comment|// Close tables (returns tables to the pool)
name|table1
operator|.
name|close
argument_list|()
expr_stmt|;
name|table2
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Request tables of the same names
name|Table
name|sameTable1
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|Table
name|sameTable2
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|otherTable
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|table1
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|,
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|sameTable1
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|table2
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|,
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|sameTable2
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testProxyImplementationReturned
parameter_list|()
block|{
name|HTablePool
name|pool
init|=
operator|new
name|HTablePool
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|String
name|tableName
init|=
name|TABLENAME
decl_stmt|;
comment|// Request a table from
comment|// an
comment|// empty pool
name|Table
name|table
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// Test if proxy implementation is returned
name|Assert
operator|.
name|assertTrue
argument_list|(
name|table
operator|instanceof
name|HTablePool
operator|.
name|PooledHTable
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeprecatedUsagePattern
parameter_list|()
throws|throws
name|IOException
block|{
name|HTablePool
name|pool
init|=
operator|new
name|HTablePool
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|String
name|tableName
init|=
name|TABLENAME
decl_stmt|;
comment|// Request a table from
comment|// an
comment|// empty pool
comment|// get table will return proxy implementation
name|HTableInterface
name|table
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// put back the proxy implementation instead of closing it
name|pool
operator|.
name|putTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
comment|// Request a table of the same name
name|Table
name|sameTable
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// test no proxy over proxy created
name|Assert
operator|.
name|assertSame
argument_list|(
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|table
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|,
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|sameTable
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReturnDifferentTable
parameter_list|()
throws|throws
name|IOException
block|{
name|HTablePool
name|pool
init|=
operator|new
name|HTablePool
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|String
name|tableName
init|=
name|TABLENAME
decl_stmt|;
comment|// Request a table from
comment|// an
comment|// empty pool
comment|// get table will return proxy implementation
specifier|final
name|Table
name|table
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HTableInterface
name|alienTable
init|=
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLENAME
argument_list|)
argument_list|)
block|{
comment|// implementation doesn't matter as long the table is not from
comment|// pool
block|}
decl_stmt|;
try|try
block|{
comment|// put the wrong table in pool
name|pool
operator|.
name|putTable
argument_list|(
name|alienTable
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"alien table accepted in pool"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"alien table rejected"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHTablePoolCloseTwice
parameter_list|()
throws|throws
name|Exception
block|{
name|HTablePool
name|pool
init|=
operator|new
name|HTablePool
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|getPoolType
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|tableName
init|=
name|TABLENAME
decl_stmt|;
comment|// Request a table from an empty pool
name|Table
name|table
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|table
operator|)
operator|.
name|isOpen
argument_list|()
argument_list|)
expr_stmt|;
comment|// Close table (returns table to the pool)
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// check if the table is closed
name|Assert
operator|.
name|assertFalse
argument_list|(
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|table
operator|)
operator|.
name|isOpen
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"Should not allow table to be closed twice"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|ex
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"table cannot be closed twice"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|pool
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Category
argument_list|(
block|{
name|ClientTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
specifier|static
class|class
name|TestHTableReusablePool
extends|extends
name|TestHTablePoolType
block|{
annotation|@
name|Override
specifier|protected
name|PoolType
name|getPoolType
parameter_list|()
block|{
return|return
name|PoolType
operator|.
name|Reusable
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableWithMaxSize
parameter_list|()
throws|throws
name|Exception
block|{
name|HTablePool
name|pool
init|=
operator|new
name|HTablePool
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|2
argument_list|,
name|getPoolType
argument_list|()
argument_list|)
decl_stmt|;
comment|// Request tables from an empty pool
name|Table
name|table1
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|Table
name|table2
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|Table
name|table3
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
comment|// Close tables (returns tables to the pool)
name|table1
operator|.
name|close
argument_list|()
expr_stmt|;
name|table2
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// The pool should reject this one since it is already full
name|table3
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Request tables of the same name
name|Table
name|sameTable1
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|Table
name|sameTable2
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|Table
name|sameTable3
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|table1
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|,
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|sameTable1
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|table2
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|,
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|sameTable2
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotSame
argument_list|(
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|table3
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|,
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|sameTable3
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCloseTablePool
parameter_list|()
throws|throws
name|IOException
block|{
name|HTablePool
name|pool
init|=
operator|new
name|HTablePool
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|4
argument_list|,
name|getPoolType
argument_list|()
argument_list|)
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|TABLENAME
argument_list|)
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
block|}
name|HTableDescriptor
name|tableDescriptor
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLENAME
argument_list|)
argument_list|)
decl_stmt|;
name|tableDescriptor
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"randomFamily"
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
comment|// Request tables from an empty pool
name|Table
index|[]
name|tables
init|=
operator|new
name|Table
index|[
literal|4
index|]
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
name|tables
index|[
name|i
index|]
operator|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
block|}
name|pool
operator|.
name|closeTablePool
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
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
name|tables
index|[
name|i
index|]
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|pool
operator|.
name|getCurrentPoolSize
argument_list|(
name|TABLENAME
argument_list|)
argument_list|)
expr_stmt|;
name|pool
operator|.
name|closeTablePool
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|pool
operator|.
name|getCurrentPoolSize
argument_list|(
name|TABLENAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Category
argument_list|(
block|{
name|ClientTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
specifier|static
class|class
name|TestHTableThreadLocalPool
extends|extends
name|TestHTablePoolType
block|{
annotation|@
name|Override
specifier|protected
name|PoolType
name|getPoolType
parameter_list|()
block|{
return|return
name|PoolType
operator|.
name|ThreadLocal
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableWithMaxSize
parameter_list|()
throws|throws
name|Exception
block|{
name|HTablePool
name|pool
init|=
operator|new
name|HTablePool
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|2
argument_list|,
name|getPoolType
argument_list|()
argument_list|)
decl_stmt|;
comment|// Request tables from an empty pool
name|Table
name|table1
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|Table
name|table2
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|Table
name|table3
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
comment|// Close tables (returns tables to the pool)
name|table1
operator|.
name|close
argument_list|()
expr_stmt|;
name|table2
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// The pool should not reject this one since the number of threads
comment|//<= 2
name|table3
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Request tables of the same name
name|Table
name|sameTable1
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|Table
name|sameTable2
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|Table
name|sameTable3
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|table3
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|,
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|sameTable1
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|table3
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|,
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|sameTable2
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|table3
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|,
operator|(
operator|(
name|HTablePool
operator|.
name|PooledHTable
operator|)
name|sameTable3
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCloseTablePool
parameter_list|()
throws|throws
name|IOException
block|{
name|HTablePool
name|pool
init|=
operator|new
name|HTablePool
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|4
argument_list|,
name|getPoolType
argument_list|()
argument_list|)
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|TABLENAME
argument_list|)
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
block|}
name|HTableDescriptor
name|tableDescriptor
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLENAME
argument_list|)
argument_list|)
decl_stmt|;
name|tableDescriptor
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"randomFamily"
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
comment|// Request tables from an empty pool
name|Table
index|[]
name|tables
init|=
operator|new
name|Table
index|[
literal|4
index|]
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
name|tables
index|[
name|i
index|]
operator|=
name|pool
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
block|}
name|pool
operator|.
name|closeTablePool
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
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
name|tables
index|[
name|i
index|]
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|pool
operator|.
name|getCurrentPoolSize
argument_list|(
name|TABLENAME
argument_list|)
argument_list|)
expr_stmt|;
name|pool
operator|.
name|closeTablePool
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|pool
operator|.
name|getCurrentPoolSize
argument_list|(
name|TABLENAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

