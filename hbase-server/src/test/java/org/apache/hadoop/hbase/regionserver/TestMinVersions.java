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
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|Delete
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
name|Get
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
name|filter
operator|.
name|TimestampsFilter
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
name|EnvironmentEdgeManager
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

begin_comment
comment|/**  * Test Minimum Versions feature (HBASE-4071).  */
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
name|TestMinVersions
extends|extends
name|HBaseTestCase
block|{
specifier|private
specifier|final
name|byte
index|[]
name|T0
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"0"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|T1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|T2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|T3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"3"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|T4
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"4"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|T5
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"5"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|c0
init|=
name|COLUMNS
index|[
literal|0
index|]
decl_stmt|;
comment|/**    * Verify behavior of getClosestBefore(...)    */
specifier|public
name|void
name|testGetClosestBefore
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|1000
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|createNewHRegion
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
comment|// 2s in the past
name|long
name|ts
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|-
literal|2000
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T1
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
operator|+
literal|1
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T4
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T3
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T3
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// now make sure that getClosestBefore(...) get can
comment|// rows that would be expired without minVersion.
comment|// also make sure it gets the latest version
name|Result
name|r
init|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T1
argument_list|,
name|c0
argument_list|)
decl_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T4
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T2
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T4
argument_list|)
expr_stmt|;
comment|// now flush/compact
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|region
operator|.
name|compactStores
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T1
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T4
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T2
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T4
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test mixed memstore and storefile scanning    * with minimum versions.    */
specifier|public
name|void
name|testStoreMemStore
parameter_list|()
throws|throws
name|Exception
block|{
comment|// keep 3 versions minimum
name|HTableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|,
literal|3
argument_list|,
literal|1000
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|createNewHRegion
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// 2s in the past
name|long
name|ts
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|-
literal|2000
decl_stmt|;
try|try
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
operator|-
literal|1
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T2
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
operator|-
literal|3
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T0
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// now flush/compact
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|region
operator|.
name|compactStores
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T3
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
operator|-
literal|2
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T1
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
operator|-
literal|3
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T0
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// newest version in the memstore
comment|// the 2nd oldest in the store file
comment|// and the 3rd, 4th oldest also in the memstore
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
decl_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|Result
name|r
init|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
comment|// this'll use ScanWildcardColumnTracker
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T3
argument_list|,
name|T2
argument_list|,
name|T1
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
expr_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|g
operator|.
name|addColumn
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
comment|// this'll use ExplicitColumnTracker
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T3
argument_list|,
name|T2
argument_list|,
name|T1
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Make sure the Deletes behave as expected with minimum versions    */
specifier|public
name|void
name|testDelete
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|,
literal|3
argument_list|,
literal|1000
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|createNewHRegion
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// 2s in the past
name|long
name|ts
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|-
literal|2000
decl_stmt|;
try|try
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
operator|-
literal|2
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T1
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
operator|-
literal|1
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T2
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T3
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|T1
argument_list|,
name|ts
operator|-
literal|1
argument_list|)
decl_stmt|;
name|region
operator|.
name|delete
argument_list|(
name|d
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
decl_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|Result
name|r
init|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
comment|// this'll use ScanWildcardColumnTracker
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T3
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
expr_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|g
operator|.
name|addColumn
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
comment|// this'll use ExplicitColumnTracker
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T3
argument_list|)
expr_stmt|;
comment|// now flush/compact
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|region
operator|.
name|compactStores
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// try again
name|g
operator|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
expr_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
comment|// this'll use ScanWildcardColumnTracker
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T3
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
expr_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|g
operator|.
name|addColumn
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
comment|// this'll use ExplicitColumnTracker
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T3
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Make sure the memstor behaves correctly with minimum versions    */
specifier|public
name|void
name|testMemStore
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|,
literal|2
argument_list|,
literal|1000
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|createNewHRegion
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// 2s in the past
name|long
name|ts
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|-
literal|2000
decl_stmt|;
try|try
block|{
comment|// 2nd version
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
operator|-
literal|2
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T2
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// 3rd version
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
operator|-
literal|1
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T3
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// 4th version
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T4
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// now flush/compact
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|region
operator|.
name|compactStores
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// now put the first version (backdated)
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
operator|-
literal|3
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T1
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// now the latest change is in the memstore,
comment|// but it is not the latest version
name|Result
name|r
init|=
name|region
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
argument_list|)
decl_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T4
argument_list|)
expr_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
decl_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
comment|// this'll use ScanWildcardColumnTracker
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T4
argument_list|,
name|T3
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
expr_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|g
operator|.
name|addColumn
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
comment|// this'll use ExplicitColumnTracker
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T4
argument_list|,
name|T3
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
operator|+
literal|1
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T5
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// now the latest version is in the memstore
name|g
operator|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
expr_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
comment|// this'll use ScanWildcardColumnTracker
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T5
argument_list|,
name|T4
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
expr_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|g
operator|.
name|addColumn
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
comment|// this'll use ExplicitColumnTracker
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T5
argument_list|,
name|T4
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Verify basic minimum versions functionality    */
specifier|public
name|void
name|testBaseCase
parameter_list|()
throws|throws
name|Exception
block|{
comment|// 1 version minimum, 1000 versions maximum, ttl = 1s
name|HTableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|,
literal|2
argument_list|,
literal|1000
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|createNewHRegion
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
comment|// 2s in the past
name|long
name|ts
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|-
literal|2000
decl_stmt|;
comment|// 1st version
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
operator|-
literal|3
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T1
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// 2nd version
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
operator|-
literal|2
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T2
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// 3rd version
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
operator|-
literal|1
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T3
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// 4th version
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T4
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|region
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
argument_list|)
decl_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T4
argument_list|)
expr_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
decl_stmt|;
name|g
operator|.
name|setTimeRange
argument_list|(
literal|0L
argument_list|,
name|ts
operator|+
literal|1
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T4
argument_list|)
expr_stmt|;
comment|// oldest version still exists
name|g
operator|.
name|setTimeRange
argument_list|(
literal|0L
argument_list|,
name|ts
operator|-
literal|2
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T1
argument_list|)
expr_stmt|;
comment|// gets see only available versions
comment|// even before compactions
name|g
operator|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
expr_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
comment|// this'll use ScanWildcardColumnTracker
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T4
argument_list|,
name|T3
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
expr_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|g
operator|.
name|addColumn
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
comment|// this'll use ExplicitColumnTracker
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T4
argument_list|,
name|T3
argument_list|)
expr_stmt|;
comment|// now flush
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// with HBASE-4241 a flush will eliminate the expired rows
name|g
operator|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
expr_stmt|;
name|g
operator|.
name|setTimeRange
argument_list|(
literal|0L
argument_list|,
name|ts
operator|-
literal|2
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|r
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// major compaction
name|region
operator|.
name|compactStores
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// after compaction the 4th version is still available
name|g
operator|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
expr_stmt|;
name|g
operator|.
name|setTimeRange
argument_list|(
literal|0L
argument_list|,
name|ts
operator|+
literal|1
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T4
argument_list|)
expr_stmt|;
comment|// so is the 3rd
name|g
operator|.
name|setTimeRange
argument_list|(
literal|0L
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T3
argument_list|)
expr_stmt|;
comment|// but the 2nd and earlier versions are gone
name|g
operator|.
name|setTimeRange
argument_list|(
literal|0L
argument_list|,
name|ts
operator|-
literal|1
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|r
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Verify that basic filters still behave correctly with    * minimum versions enabled.    */
specifier|public
name|void
name|testFilters
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|,
literal|2
argument_list|,
literal|1000
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|createNewHRegion
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|c1
init|=
name|COLUMNS
index|[
literal|1
index|]
decl_stmt|;
comment|// 2s in the past
name|long
name|ts
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|-
literal|2000
decl_stmt|;
try|try
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
operator|-
literal|3
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T0
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c1
argument_list|,
name|c1
argument_list|,
name|T0
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
operator|-
literal|2
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T1
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c1
argument_list|,
name|c1
argument_list|,
name|T1
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
operator|-
literal|1
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T2
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c1
argument_list|,
name|c1
argument_list|,
name|T2
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T1
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T3
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c1
argument_list|,
name|c1
argument_list|,
name|T3
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|tss
init|=
operator|new
name|ArrayList
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|tss
operator|.
name|add
argument_list|(
name|ts
operator|-
literal|1
argument_list|)
expr_stmt|;
name|tss
operator|.
name|add
argument_list|(
name|ts
operator|-
literal|2
argument_list|)
expr_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
decl_stmt|;
name|g
operator|.
name|addColumn
argument_list|(
name|c1
argument_list|,
name|c1
argument_list|)
expr_stmt|;
name|g
operator|.
name|setFilter
argument_list|(
operator|new
name|TimestampsFilter
argument_list|(
name|tss
argument_list|)
argument_list|)
expr_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|Result
name|r
init|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|c1
argument_list|,
name|T2
argument_list|,
name|T1
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
expr_stmt|;
name|g
operator|.
name|addColumn
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|g
operator|.
name|setFilter
argument_list|(
operator|new
name|TimestampsFilter
argument_list|(
name|tss
argument_list|)
argument_list|)
expr_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T2
argument_list|,
name|T1
argument_list|)
expr_stmt|;
comment|// now flush/compact
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|region
operator|.
name|compactStores
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
expr_stmt|;
name|g
operator|.
name|addColumn
argument_list|(
name|c1
argument_list|,
name|c1
argument_list|)
expr_stmt|;
name|g
operator|.
name|setFilter
argument_list|(
operator|new
name|TimestampsFilter
argument_list|(
name|tss
argument_list|)
argument_list|)
expr_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|c1
argument_list|,
name|T2
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|T1
argument_list|)
expr_stmt|;
name|g
operator|.
name|addColumn
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|g
operator|.
name|setFilter
argument_list|(
operator|new
name|TimestampsFilter
argument_list|(
name|tss
argument_list|)
argument_list|)
expr_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|c0
argument_list|,
name|T2
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|checkResult
parameter_list|(
name|Result
name|r
parameter_list|,
name|byte
index|[]
name|col
parameter_list|,
name|byte
index|[]
modifier|...
name|vals
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|r
operator|.
name|size
argument_list|()
argument_list|,
name|vals
operator|.
name|length
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
name|r
operator|.
name|getColumn
argument_list|(
name|col
argument_list|,
name|col
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|kvs
operator|.
name|size
argument_list|()
argument_list|,
name|vals
operator|.
name|length
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
name|vals
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|kvs
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|,
name|vals
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

