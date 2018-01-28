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
operator|.
name|wal
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
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseClassTestRule
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
name|MiscTests
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
name|ClassRule
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
name|MiscTests
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
name|TestMetricsWAL
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestMetricsWAL
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testLogRollRequested
parameter_list|()
throws|throws
name|Exception
block|{
name|MetricsWALSource
name|source
init|=
name|mock
argument_list|(
name|MetricsWALSourceImpl
operator|.
name|class
argument_list|)
decl_stmt|;
name|MetricsWAL
name|metricsWAL
init|=
operator|new
name|MetricsWAL
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|metricsWAL
operator|.
name|logRollRequested
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|metricsWAL
operator|.
name|logRollRequested
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Log roll was requested twice
name|verify
argument_list|(
name|source
argument_list|,
name|times
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|incrementLogRollRequested
argument_list|()
expr_stmt|;
comment|// One was because of low replication on the hlog.
name|verify
argument_list|(
name|source
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|incrementLowReplicationLogRoll
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPostSync
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|nanos
init|=
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toNanos
argument_list|(
literal|145
argument_list|)
decl_stmt|;
name|MetricsWALSource
name|source
init|=
name|mock
argument_list|(
name|MetricsWALSourceImpl
operator|.
name|class
argument_list|)
decl_stmt|;
name|MetricsWAL
name|metricsWAL
init|=
operator|new
name|MetricsWAL
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|metricsWAL
operator|.
name|postSync
argument_list|(
name|nanos
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|source
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|incrementSyncTime
argument_list|(
literal|145
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSlowAppend
parameter_list|()
throws|throws
name|Exception
block|{
name|MetricsWALSource
name|source
init|=
operator|new
name|MetricsWALSourceImpl
argument_list|()
decl_stmt|;
name|MetricsWAL
name|metricsWAL
init|=
operator|new
name|MetricsWAL
argument_list|(
name|source
argument_list|)
decl_stmt|;
comment|// One not so slow append (< 1000)
name|metricsWAL
operator|.
name|postAppend
argument_list|(
literal|1
argument_list|,
literal|900
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Two slow appends (> 1000)
name|metricsWAL
operator|.
name|postAppend
argument_list|(
literal|1
argument_list|,
literal|1010
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|metricsWAL
operator|.
name|postAppend
argument_list|(
literal|1
argument_list|,
literal|2000
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|source
operator|.
name|getSlowAppendCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWalWrittenInBytes
parameter_list|()
throws|throws
name|Exception
block|{
name|MetricsWALSource
name|source
init|=
name|mock
argument_list|(
name|MetricsWALSourceImpl
operator|.
name|class
argument_list|)
decl_stmt|;
name|MetricsWAL
name|metricsWAL
init|=
operator|new
name|MetricsWAL
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|metricsWAL
operator|.
name|postAppend
argument_list|(
literal|100
argument_list|,
literal|900
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|metricsWAL
operator|.
name|postAppend
argument_list|(
literal|200
argument_list|,
literal|2000
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|source
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|incrementWrittenBytes
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|source
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|incrementWrittenBytes
argument_list|(
literal|200
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

