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
name|io
operator|.
name|hfile
operator|.
name|HFile
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
name|IntegrationTests
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
name|LoadTestDataGeneratorWithTags
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
name|LoadTestTool
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
specifier|private
specifier|static
specifier|final
name|char
name|COLON
init|=
literal|':'
decl_stmt|;
specifier|private
name|int
name|minTagsPerKey
init|=
literal|1
decl_stmt|,
name|maxTagsPerKey
init|=
literal|10
decl_stmt|;
specifier|private
name|int
name|minTagLength
init|=
literal|16
decl_stmt|,
name|maxTagLength
init|=
literal|512
decl_stmt|;
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
name|HFile
operator|.
name|FORMAT_VERSION_KEY
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
name|String
index|[]
name|getArgsForLoadTestTool
parameter_list|(
name|String
name|mode
parameter_list|,
name|String
name|modeSpecificArg
parameter_list|,
name|long
name|startKey
parameter_list|,
name|long
name|numKeys
parameter_list|)
block|{
name|String
index|[]
name|args
init|=
name|super
operator|.
name|getArgsForLoadTestTool
argument_list|(
name|mode
argument_list|,
name|modeSpecificArg
argument_list|,
name|startKey
argument_list|,
name|numKeys
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|tmp
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|args
argument_list|)
argument_list|)
decl_stmt|;
comment|// LoadTestDataGeneratorWithTags:minNumTags:maxNumTags:minTagLength:maxTagLength
name|tmp
operator|.
name|add
argument_list|(
name|HIPHEN
operator|+
name|LoadTestTool
operator|.
name|OPT_GENERATOR
argument_list|)
expr_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
name|LoadTestDataGeneratorWithTags
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COLON
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|minTagsPerKey
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COLON
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|maxTagsPerKey
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COLON
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|minTagLength
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COLON
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|maxTagLength
argument_list|)
expr_stmt|;
name|tmp
operator|.
name|add
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|tmp
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|tmp
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
block|}
end_class

end_unit

