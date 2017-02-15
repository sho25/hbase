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
name|coprocessor
package|;
end_package

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
name|HBaseTestingUtility
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
name|Durability
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
name|Put
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
name|Result
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
name|ResultScanner
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
name|Scan
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
name|regionserver
operator|.
name|wal
operator|.
name|WALEdit
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
name|CoprocessorTests
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
name|Threads
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|SynchronousQueue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadPoolExecutor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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

begin_comment
comment|/**  * Test that a coprocessor can open a connection and write to another table, inside a hook.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|CoprocessorTests
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
name|TestOpenTableInCoprocessor
block|{
specifier|private
specifier|static
specifier|final
name|TableName
name|otherTable
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"otherTable"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|primaryTable
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"primary"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|family
init|=
operator|new
name|byte
index|[]
block|{
literal|'f'
block|}
decl_stmt|;
specifier|private
specifier|static
name|boolean
index|[]
name|completed
init|=
operator|new
name|boolean
index|[
literal|1
index|]
decl_stmt|;
comment|/**    * Custom coprocessor that just copies the write to another table.    */
specifier|public
specifier|static
class|class
name|SendToOtherTableCoprocessor
implements|implements
name|RegionObserver
block|{
annotation|@
name|Override
specifier|public
name|void
name|prePut
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Put
name|put
parameter_list|,
specifier|final
name|WALEdit
name|edit
parameter_list|,
specifier|final
name|Durability
name|durability
parameter_list|)
throws|throws
name|IOException
block|{
name|Table
name|table
init|=
name|e
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getTable
argument_list|(
name|otherTable
argument_list|)
decl_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|completed
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|boolean
index|[]
name|completedWithPool
init|=
operator|new
name|boolean
index|[
literal|1
index|]
decl_stmt|;
comment|/**    * Coprocessor that creates an HTable with a pool to write to another table    */
specifier|public
specifier|static
class|class
name|CustomThreadPoolCoprocessor
implements|implements
name|RegionObserver
block|{
comment|/**      * Get a pool that has only ever one thread. A second action added to the pool (running      * concurrently), will cause an exception.      * @return      */
specifier|private
name|ExecutorService
name|getPool
parameter_list|()
block|{
name|int
name|maxThreads
init|=
literal|1
decl_stmt|;
name|long
name|keepAliveTime
init|=
literal|60
decl_stmt|;
name|ThreadPoolExecutor
name|pool
init|=
operator|new
name|ThreadPoolExecutor
argument_list|(
literal|1
argument_list|,
name|maxThreads
argument_list|,
name|keepAliveTime
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
operator|new
name|SynchronousQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
literal|"hbase-table"
argument_list|)
argument_list|)
decl_stmt|;
name|pool
operator|.
name|allowCoreThreadTimeOut
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|pool
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|prePut
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Put
name|put
parameter_list|,
specifier|final
name|WALEdit
name|edit
parameter_list|,
specifier|final
name|Durability
name|durability
parameter_list|)
throws|throws
name|IOException
block|{
name|Table
name|table
init|=
name|e
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getTable
argument_list|(
name|otherTable
argument_list|,
name|getPool
argument_list|()
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|'a'
block|}
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
literal|null
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|'a'
block|}
argument_list|)
expr_stmt|;
try|try
block|{
name|table
operator|.
name|batch
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|put
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e1
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e1
argument_list|)
throw|;
block|}
name|completedWithPool
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|cleanupTestTable
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|primaryTable
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|deleteTable
argument_list|(
name|primaryTable
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|otherTable
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|deleteTable
argument_list|(
name|otherTable
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|teardownCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCoprocessorCanCreateConnectionToRemoteTable
parameter_list|()
throws|throws
name|Throwable
block|{
name|runCoprocessorConnectionToRemoteTable
argument_list|(
name|SendToOtherTableCoprocessor
operator|.
name|class
argument_list|,
name|completed
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCoprocessorCanCreateConnectionToRemoteTableWithCustomPool
parameter_list|()
throws|throws
name|Throwable
block|{
name|runCoprocessorConnectionToRemoteTable
argument_list|(
name|CustomThreadPoolCoprocessor
operator|.
name|class
argument_list|,
name|completedWithPool
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runCoprocessorConnectionToRemoteTable
parameter_list|(
name|Class
name|clazz
parameter_list|,
name|boolean
index|[]
name|completeCheck
parameter_list|)
throws|throws
name|Throwable
block|{
comment|// Check if given class implements RegionObserver.
assert|assert
operator|(
name|RegionObserver
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|clazz
argument_list|)
operator|)
assert|;
name|HTableDescriptor
name|primary
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|primaryTable
argument_list|)
decl_stmt|;
name|primary
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
comment|// add our coprocessor
name|primary
operator|.
name|addCoprocessor
argument_list|(
name|clazz
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|other
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|otherTable
argument_list|)
decl_stmt|;
name|other
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|primary
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|other
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"primary"
argument_list|)
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|'a'
block|}
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
literal|null
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|'a'
block|}
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|Table
name|target
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|otherTable
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Didn't complete update to target table!"
argument_list|,
name|completeCheck
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Didn't find inserted row"
argument_list|,
literal|1
argument_list|,
name|getKeyValueCount
argument_list|(
name|target
argument_list|)
argument_list|)
expr_stmt|;
name|target
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Count the number of keyvalue in the table. Scans all possible versions    * @param table table to scan    * @return number of keyvalues over all rows in the table    * @throws IOException    */
specifier|private
name|int
name|getKeyValueCount
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
operator|-
literal|1
argument_list|)
expr_stmt|;
name|ResultScanner
name|results
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|results
control|)
block|{
name|count
operator|+=
name|res
operator|.
name|listCells
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|count
operator|+
literal|") "
operator|+
name|res
argument_list|)
expr_stmt|;
block|}
name|results
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|count
return|;
block|}
block|}
end_class

end_unit

