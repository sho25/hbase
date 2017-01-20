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
name|security
operator|.
name|visibility
operator|.
name|LoadTestDataGeneratorWithVisibilityLabels
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
name|visibility
operator|.
name|VisibilityClient
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
name|visibility
operator|.
name|VisibilityController
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
name|IntegrationTestIngestWithVisibilityLabels
extends|extends
name|IntegrationTestIngest
block|{
specifier|private
specifier|static
specifier|final
name|char
name|COMMA
init|=
literal|','
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|char
name|COLON
init|=
literal|':'
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|LABELS
init|=
block|{
literal|"secret"
block|,
literal|"topsecret"
block|,
literal|"confidential"
block|,
literal|"public"
block|,
literal|"private"
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|VISIBILITY_EXPS
init|=
block|{
literal|"secret& confidential& !private"
block|,
literal|"topsecret | confidential"
block|,
literal|"confidential& private"
block|,
literal|"public"
block|,
literal|"topsecret& private"
block|,
literal|"!public | private"
block|,
literal|"(secret | topsecret)& private"
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|AUTHS
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|tmp
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|tmp
operator|.
name|add
argument_list|(
literal|"secret"
argument_list|)
expr_stmt|;
name|tmp
operator|.
name|add
argument_list|(
literal|"confidential"
argument_list|)
expr_stmt|;
name|AUTHS
operator|.
name|add
argument_list|(
name|tmp
argument_list|)
expr_stmt|;
name|tmp
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|tmp
operator|.
name|add
argument_list|(
literal|"topsecret"
argument_list|)
expr_stmt|;
name|AUTHS
operator|.
name|add
argument_list|(
name|tmp
argument_list|)
expr_stmt|;
name|tmp
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|tmp
operator|.
name|add
argument_list|(
literal|"confidential"
argument_list|)
expr_stmt|;
name|tmp
operator|.
name|add
argument_list|(
literal|"private"
argument_list|)
expr_stmt|;
name|AUTHS
operator|.
name|add
argument_list|(
name|tmp
argument_list|)
expr_stmt|;
name|tmp
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|tmp
operator|.
name|add
argument_list|(
literal|"public"
argument_list|)
expr_stmt|;
name|AUTHS
operator|.
name|add
argument_list|(
name|tmp
argument_list|)
expr_stmt|;
name|tmp
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|tmp
operator|.
name|add
argument_list|(
literal|"topsecret"
argument_list|)
expr_stmt|;
name|tmp
operator|.
name|add
argument_list|(
literal|"private"
argument_list|)
expr_stmt|;
name|AUTHS
operator|.
name|add
argument_list|(
name|tmp
argument_list|)
expr_stmt|;
name|tmp
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|tmp
operator|.
name|add
argument_list|(
literal|"confidential"
argument_list|)
expr_stmt|;
name|AUTHS
operator|.
name|add
argument_list|(
name|tmp
argument_list|)
expr_stmt|;
name|tmp
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|tmp
operator|.
name|add
argument_list|(
literal|"topsecret"
argument_list|)
expr_stmt|;
name|tmp
operator|.
name|add
argument_list|(
literal|"private"
argument_list|)
expr_stmt|;
name|AUTHS
operator|.
name|add
argument_list|(
name|tmp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setUpCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|util
operator|=
name|getTestingUtil
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
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
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.coprocessor.master.classes"
argument_list|,
name|VisibilityController
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.coprocessor.region.classes"
argument_list|,
name|VisibilityController
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.superuser"
argument_list|,
literal|"admin,"
operator|+
name|User
operator|.
name|getCurrent
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|setUpCluster
argument_list|()
expr_stmt|;
name|addLabels
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
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|args
argument_list|)
argument_list|)
decl_stmt|;
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
name|LoadTestDataGeneratorWithVisibilityLabels
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
name|asCommaSeperatedString
argument_list|(
name|VISIBILITY_EXPS
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COLON
argument_list|)
expr_stmt|;
name|String
name|authorizationsStr
init|=
name|AUTHS
operator|.
name|toString
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|authorizationsStr
operator|.
name|substring
argument_list|(
literal|1
argument_list|,
name|authorizationsStr
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
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
specifier|private
specifier|static
name|String
name|asCommaSeperatedString
parameter_list|(
name|String
index|[]
name|list
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|item
range|:
name|list
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|item
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COMMA
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sb
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// Remove the trailing ,
name|sb
operator|.
name|deleteCharAt
argument_list|(
name|sb
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|void
name|addLabels
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|VisibilityClient
operator|.
name|addLabels
argument_list|(
name|util
operator|.
name|getConnection
argument_list|()
argument_list|,
name|LABELS
argument_list|)
expr_stmt|;
name|VisibilityClient
operator|.
name|setAuths
argument_list|(
name|util
operator|.
name|getConnection
argument_list|()
argument_list|,
name|LABELS
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|t
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

