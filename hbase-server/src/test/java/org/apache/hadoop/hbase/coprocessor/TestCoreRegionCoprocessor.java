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
name|coprocessor
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
name|MockRegionServerServices
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
name|ColumnFamilyDescriptor
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
name|ColumnFamilyDescriptorBuilder
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
name|RegionInfo
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
name|RegionInfoBuilder
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
name|TableDescriptor
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
name|TableDescriptorBuilder
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
name|regionserver
operator|.
name|RegionCoprocessorHost
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
name|RegionServerServices
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
name|util
operator|.
name|Bytes
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
name|Before
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
name|Rule
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
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestName
import|;
end_import

begin_comment
comment|/**  * Test CoreCoprocessor Annotation works giving access to facility not usually available.  * Test RegionCoprocessor.  */
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCoreRegionCoprocessor
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
name|TestCoreRegionCoprocessor
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
name|HBaseTestingUtility
name|HTU
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
name|HRegion
name|region
init|=
literal|null
decl_stmt|;
specifier|private
name|RegionServerServices
name|rss
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|methodName
init|=
name|this
operator|.
name|name
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|methodName
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptor
name|cfd
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|methodName
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|TableDescriptor
name|td
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tn
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|cfd
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
name|ri
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tn
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|this
operator|.
name|rss
operator|=
operator|new
name|MockRegionServerServices
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|region
operator|=
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|ri
argument_list|,
name|td
argument_list|,
literal|null
argument_list|,
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|this
operator|.
name|rss
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|after
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * No annotation with CoreCoprocessor. This should make it so I can NOT get at instance of a    * RegionServerServices instance after some gymnastics.    */
specifier|public
specifier|static
class|class
name|NotCoreRegionCoprocessor
implements|implements
name|RegionCoprocessor
block|{
specifier|public
name|NotCoreRegionCoprocessor
parameter_list|()
block|{}
block|}
comment|/**    * Annotate with CoreCoprocessor. This should make it so I can get at instance of a    * RegionServerServices instance after some gymnastics.    */
annotation|@
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|coprocessor
operator|.
name|CoreCoprocessor
specifier|public
specifier|static
class|class
name|CoreRegionCoprocessor
implements|implements
name|RegionCoprocessor
block|{
specifier|public
name|CoreRegionCoprocessor
parameter_list|()
block|{}
block|}
comment|/**    * Assert that when a Coprocessor is annotated with CoreCoprocessor, then it is possible to    * access a RegionServerServices instance. Assert the opposite too.    * Do it to RegionCoprocessors.    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testCoreRegionCoprocessor
parameter_list|()
throws|throws
name|IOException
block|{
name|RegionCoprocessorHost
name|rch
init|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
name|RegionCoprocessorEnvironment
name|env
init|=
name|rch
operator|.
name|load
argument_list|(
literal|null
argument_list|,
name|NotCoreRegionCoprocessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
literal|0
argument_list|,
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|env
operator|instanceof
name|HasRegionServerServices
argument_list|)
expr_stmt|;
name|env
operator|=
name|rch
operator|.
name|load
argument_list|(
literal|null
argument_list|,
name|CoreRegionCoprocessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
literal|1
argument_list|,
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|env
operator|instanceof
name|HasRegionServerServices
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|this
operator|.
name|rss
argument_list|,
operator|(
operator|(
name|HasRegionServerServices
operator|)
name|env
operator|)
operator|.
name|getRegionServerServices
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

