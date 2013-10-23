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
package|;
end_package

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
name|IntegrationTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|IntegrationTestIngestWithTags
extends|extends
name|IntegrationTestIngest
block|{
annotation|@
name|Override
specifier|public
name|void
name|setUpCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|getTestingUtil
argument_list|(
name|conf
argument_list|)
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|super
operator|.
name|setUpCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|runIngestTest
parameter_list|(
name|long
name|defaultRunTime
parameter_list|,
name|int
name|keysPerServerPerIter
parameter_list|,
name|int
name|colsPerKey
parameter_list|,
name|int
name|recordSize
parameter_list|,
name|int
name|writeThreads
parameter_list|,
name|boolean
name|useTags
parameter_list|,
name|int
name|maxTagsPerKey
parameter_list|)
throws|throws
name|Exception
block|{
name|super
operator|.
name|runIngestTest
argument_list|(
name|defaultRunTime
argument_list|,
name|keysPerServerPerIter
argument_list|,
name|colsPerKey
argument_list|,
name|recordSize
argument_list|,
name|writeThreads
argument_list|,
literal|true
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

