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
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Modifier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|runners
operator|.
name|Suite
import|;
end_import

begin_comment
comment|/**  * ClassFinder that is pre-configured with filters that will only allow test classes.  * The name is strange because a logical name would start with "Test" and be confusing.  */
end_comment

begin_class
specifier|public
class|class
name|ClassTestFinder
extends|extends
name|ClassFinder
block|{
specifier|public
name|ClassTestFinder
parameter_list|()
block|{
name|super
argument_list|(
operator|new
name|TestFileNameFilter
argument_list|()
argument_list|,
operator|new
name|TestFileNameFilter
argument_list|()
argument_list|,
operator|new
name|TestClassFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ClassTestFinder
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|category
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|TestFileNameFilter
argument_list|()
argument_list|,
operator|new
name|TestFileNameFilter
argument_list|()
argument_list|,
operator|new
name|TestClassFilter
argument_list|(
name|category
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|getCategoryAnnotations
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
name|Category
name|category
init|=
name|c
operator|.
name|getAnnotation
argument_list|(
name|Category
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|category
operator|!=
literal|null
condition|)
block|{
return|return
name|category
operator|.
name|value
argument_list|()
return|;
block|}
return|return
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[
literal|0
index|]
return|;
block|}
comment|/** Filters both test classes and anything in the hadoop-compat modules */
specifier|public
specifier|static
class|class
name|TestFileNameFilter
implements|implements
name|FileNameFilter
implements|,
name|ResourcePathFilter
block|{
specifier|private
specifier|static
specifier|final
name|Pattern
name|hadoopCompactRe
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"hbase-hadoop\\d?-compat"
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|isCandidateFile
parameter_list|(
name|String
name|fileName
parameter_list|,
name|String
name|absFilePath
parameter_list|)
block|{
name|boolean
name|isTestFile
init|=
name|fileName
operator|.
name|startsWith
argument_list|(
literal|"Test"
argument_list|)
operator|||
name|fileName
operator|.
name|startsWith
argument_list|(
literal|"IntegrationTest"
argument_list|)
decl_stmt|;
return|return
name|isTestFile
operator|&&
operator|!
name|hadoopCompactRe
operator|.
name|matcher
argument_list|(
name|absFilePath
argument_list|)
operator|.
name|find
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isCandidatePath
parameter_list|(
name|String
name|resourcePath
parameter_list|,
name|boolean
name|isJar
parameter_list|)
block|{
return|return
operator|!
name|hadoopCompactRe
operator|.
name|matcher
argument_list|(
name|resourcePath
argument_list|)
operator|.
name|find
argument_list|()
return|;
block|}
block|}
empty_stmt|;
comment|/*   * A class is considered as a test class if:    *  - it's not Abstract AND    *  - one or more of its methods is annotated with org.junit.Test OR    *  - the class is annotated with Suite.SuiteClasses   * */
specifier|public
specifier|static
class|class
name|TestClassFilter
implements|implements
name|ClassFilter
block|{
specifier|private
name|Class
argument_list|<
name|?
argument_list|>
name|categoryAnnotation
init|=
literal|null
decl_stmt|;
specifier|public
name|TestClassFilter
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|categoryAnnotation
parameter_list|)
block|{
name|this
operator|.
name|categoryAnnotation
operator|=
name|categoryAnnotation
expr_stmt|;
block|}
specifier|public
name|TestClassFilter
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isCandidateClass
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
return|return
name|isTestClass
argument_list|(
name|c
argument_list|)
operator|&&
name|isCategorizedClass
argument_list|(
name|c
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|isTestClass
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
if|if
condition|(
name|Modifier
operator|.
name|isAbstract
argument_list|(
name|c
operator|.
name|getModifiers
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|c
operator|.
name|getAnnotation
argument_list|(
name|Suite
operator|.
name|SuiteClasses
operator|.
name|class
argument_list|)
operator|!=
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
for|for
control|(
name|Method
name|met
range|:
name|c
operator|.
name|getMethods
argument_list|()
control|)
block|{
if|if
condition|(
name|met
operator|.
name|getAnnotation
argument_list|(
name|Test
operator|.
name|class
argument_list|)
operator|!=
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|boolean
name|isCategorizedClass
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|categoryAnnotation
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|cc
range|:
name|getCategoryAnnotations
argument_list|(
name|c
argument_list|)
control|)
block|{
if|if
condition|(
name|cc
operator|.
name|equals
argument_list|(
name|this
operator|.
name|categoryAnnotation
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
empty_stmt|;
block|}
end_class

begin_empty_stmt
empty_stmt|;
end_empty_stmt

end_unit

