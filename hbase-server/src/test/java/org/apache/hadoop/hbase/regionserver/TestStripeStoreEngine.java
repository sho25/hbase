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
name|regionserver
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|anyInt
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|anyLong
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
name|times
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
name|verify
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
name|ArrayList
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
name|CellComparator
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
name|regionserver
operator|.
name|compactions
operator|.
name|CompactionContext
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
name|compactions
operator|.
name|CompactionRequest
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
name|compactions
operator|.
name|StripeCompactionPolicy
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
name|compactions
operator|.
name|StripeCompactor
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
name|throttle
operator|.
name|NoLimitThroughputController
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
name|throttle
operator|.
name|ThroughputController
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
name|security
operator|.
name|User
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
name|RegionServerTests
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
block|{
name|RegionServerTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestStripeStoreEngine
block|{
annotation|@
name|Test
specifier|public
name|void
name|testCreateBasedOnConfig
parameter_list|()
throws|throws
name|Exception
block|{
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
name|set
argument_list|(
name|StoreEngine
operator|.
name|STORE_ENGINE_CLASS_KEY
argument_list|,
name|TestStoreEngine
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|StripeStoreEngine
name|se
init|=
name|createEngine
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|se
operator|.
name|getCompactionPolicy
argument_list|()
operator|instanceof
name|StripeCompactionPolicy
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|TestStoreEngine
extends|extends
name|StripeStoreEngine
block|{
specifier|public
name|void
name|setCompactorOverride
parameter_list|(
name|StripeCompactor
name|compactorOverride
parameter_list|)
block|{
name|this
operator|.
name|compactor
operator|=
name|compactorOverride
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompactionContextForceSelect
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|int
name|targetCount
init|=
literal|2
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|StripeStoreConfig
operator|.
name|INITIAL_STRIPE_COUNT_KEY
argument_list|,
name|targetCount
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|StripeStoreConfig
operator|.
name|MIN_FILES_L0_KEY
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|StoreEngine
operator|.
name|STORE_ENGINE_CLASS_KEY
argument_list|,
name|TestStoreEngine
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|TestStoreEngine
name|se
init|=
name|createEngine
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|StripeCompactor
name|mockCompactor
init|=
name|mock
argument_list|(
name|StripeCompactor
operator|.
name|class
argument_list|)
decl_stmt|;
name|se
operator|.
name|setCompactorOverride
argument_list|(
name|mockCompactor
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockCompactor
operator|.
name|compact
argument_list|(
name|any
argument_list|(
name|CompactionRequest
operator|.
name|class
argument_list|)
argument_list|,
name|anyInt
argument_list|()
argument_list|,
name|anyLong
argument_list|()
argument_list|,
name|any
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|ThroughputController
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|User
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
comment|// Produce 3 L0 files.
name|StoreFile
name|sf
init|=
name|createFile
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|compactUs
init|=
name|al
argument_list|(
name|sf
argument_list|,
name|createFile
argument_list|()
argument_list|,
name|createFile
argument_list|()
argument_list|)
decl_stmt|;
name|se
operator|.
name|getStoreFileManager
argument_list|()
operator|.
name|loadFiles
argument_list|(
name|compactUs
argument_list|)
expr_stmt|;
comment|// Create a compaction that would want to split the stripe.
name|CompactionContext
name|compaction
init|=
name|se
operator|.
name|createCompaction
argument_list|()
decl_stmt|;
name|compaction
operator|.
name|select
argument_list|(
name|al
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|compaction
operator|.
name|getRequest
argument_list|()
operator|.
name|getFiles
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Override the file list. Granted, overriding this compaction in this manner will
comment|// break things in real world, but we only want to verify the override.
name|compactUs
operator|.
name|remove
argument_list|(
name|sf
argument_list|)
expr_stmt|;
name|CompactionRequest
name|req
init|=
operator|new
name|CompactionRequest
argument_list|(
name|compactUs
argument_list|)
decl_stmt|;
name|compaction
operator|.
name|forceSelect
argument_list|(
name|req
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|compaction
operator|.
name|getRequest
argument_list|()
operator|.
name|getFiles
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|compaction
operator|.
name|getRequest
argument_list|()
operator|.
name|getFiles
argument_list|()
operator|.
name|contains
argument_list|(
name|sf
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure the correct method it called on compactor.
name|compaction
operator|.
name|compact
argument_list|(
name|NoLimitThroughputController
operator|.
name|INSTANCE
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|mockCompactor
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|compact
argument_list|(
name|compaction
operator|.
name|getRequest
argument_list|()
argument_list|,
name|targetCount
argument_list|,
literal|0L
argument_list|,
name|StripeStoreFileManager
operator|.
name|OPEN_KEY
argument_list|,
name|StripeStoreFileManager
operator|.
name|OPEN_KEY
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|NoLimitThroughputController
operator|.
name|INSTANCE
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|StoreFile
name|createFile
parameter_list|()
throws|throws
name|Exception
block|{
name|StoreFile
name|sf
init|=
name|mock
argument_list|(
name|StoreFile
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|sf
operator|.
name|getMetadataValue
argument_list|(
name|any
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|StripeStoreFileManager
operator|.
name|INVALID_KEY
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|sf
operator|.
name|getReader
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mock
argument_list|(
name|StoreFileReader
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|sf
operator|.
name|getPath
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|Path
argument_list|(
literal|"moo"
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|sf
return|;
block|}
specifier|private
specifier|static
name|TestStoreEngine
name|createEngine
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|Store
name|store
init|=
name|mock
argument_list|(
name|Store
operator|.
name|class
argument_list|)
decl_stmt|;
name|CellComparator
name|kvComparator
init|=
name|mock
argument_list|(
name|CellComparator
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
operator|(
name|TestStoreEngine
operator|)
name|StoreEngine
operator|.
name|create
argument_list|(
name|store
argument_list|,
name|conf
argument_list|,
name|kvComparator
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|al
parameter_list|(
name|StoreFile
modifier|...
name|sfs
parameter_list|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|sfs
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

