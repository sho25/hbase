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
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|CompatibilitySingletonFactory
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

begin_class
specifier|public
class|class
name|TestMetricsRegionSourceImpl
block|{
annotation|@
name|Test
specifier|public
name|void
name|testCompareTo
parameter_list|()
throws|throws
name|Exception
block|{
name|MetricsRegionServerSourceFactory
name|fact
init|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsRegionServerSourceFactory
operator|.
name|class
argument_list|)
decl_stmt|;
name|MetricsRegionSource
name|one
init|=
name|fact
operator|.
name|createRegion
argument_list|(
operator|new
name|RegionWrapperStub
argument_list|(
literal|"TEST"
argument_list|)
argument_list|)
decl_stmt|;
name|MetricsRegionSource
name|oneClone
init|=
name|fact
operator|.
name|createRegion
argument_list|(
operator|new
name|RegionWrapperStub
argument_list|(
literal|"TEST"
argument_list|)
argument_list|)
decl_stmt|;
name|MetricsRegionSource
name|two
init|=
name|fact
operator|.
name|createRegion
argument_list|(
operator|new
name|RegionWrapperStub
argument_list|(
literal|"TWO"
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|one
operator|.
name|compareTo
argument_list|(
name|oneClone
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|one
operator|.
name|compareTo
argument_list|(
name|two
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|two
operator|.
name|compareTo
argument_list|(
name|one
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|RuntimeException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testNoGetRegionServerMetricsSourceImpl
parameter_list|()
throws|throws
name|Exception
block|{
comment|// This should throw an exception because MetricsRegionSourceImpl should only
comment|// be created by a factory.
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsRegionSource
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
class|class
name|RegionWrapperStub
implements|implements
name|MetricsRegionWrapper
block|{
specifier|private
name|String
name|regionName
decl_stmt|;
specifier|public
name|RegionWrapperStub
parameter_list|(
name|String
name|regionName
parameter_list|)
block|{
name|this
operator|.
name|regionName
operator|=
name|regionName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getNamespace
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getRegionName
parameter_list|()
block|{
return|return
name|this
operator|.
name|regionName
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumStores
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumStoreFiles
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMemstoreSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getStoreFileSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getReadRequestCount
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getWriteRequestCount
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
block|}
block|}
end_class

end_unit

