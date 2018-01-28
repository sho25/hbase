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
name|metrics
operator|.
name|impl
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|metrics
operator|.
name|Gauge
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

begin_comment
comment|/**  * Test class for {@link Gauge}.  */
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
name|TestGauge
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
name|TestGauge
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testGetValue
parameter_list|()
block|{
name|SimpleGauge
name|gauge
init|=
operator|new
name|SimpleGauge
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
operator|(
name|long
operator|)
name|gauge
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|gauge
operator|.
name|setValue
argument_list|(
literal|1000L
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1000L
argument_list|,
operator|(
name|long
operator|)
name|gauge
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Gauge implementation with a setter.    */
specifier|private
specifier|static
class|class
name|SimpleGauge
implements|implements
name|Gauge
argument_list|<
name|Long
argument_list|>
block|{
specifier|private
specifier|final
name|AtomicLong
name|value
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0L
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Long
name|getValue
parameter_list|()
block|{
return|return
name|this
operator|.
name|value
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|void
name|setValue
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|.
name|set
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

