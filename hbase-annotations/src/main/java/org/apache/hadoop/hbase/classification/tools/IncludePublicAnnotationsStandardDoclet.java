begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|classification
operator|.
name|tools
package|;
end_package

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|javadoc
operator|.
name|DocErrorReporter
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|javadoc
operator|.
name|LanguageVersion
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|javadoc
operator|.
name|RootDoc
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|tools
operator|.
name|doclets
operator|.
name|standard
operator|.
name|Standard
import|;
end_import

begin_comment
comment|/**  * A<a href="http://java.sun.com/javase/6/docs/jdk/api/javadoc/doclet/">Doclet</a>  * that only includes class-level elements that are annotated with  * {@link org.apache.hadoop.hbase.classification.InterfaceAudience.Public}.  * Class-level elements with no annotation are excluded.  * In addition, all elements that are annotated with  * {@link org.apache.hadoop.hbase.classification.InterfaceAudience.Private} or  * {@link org.apache.hadoop.hbase.classification.InterfaceAudience.LimitedPrivate}  * are also excluded.  * It delegates to the Standard Doclet, and takes the same options.  */
end_comment

begin_class
specifier|public
class|class
name|IncludePublicAnnotationsStandardDoclet
block|{
specifier|public
specifier|static
name|LanguageVersion
name|languageVersion
parameter_list|()
block|{
return|return
name|LanguageVersion
operator|.
name|JAVA_1_5
return|;
block|}
specifier|public
specifier|static
name|boolean
name|start
parameter_list|(
name|RootDoc
name|root
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|IncludePublicAnnotationsStandardDoclet
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|RootDocProcessor
operator|.
name|treatUnannotatedClassesAsPrivate
operator|=
literal|true
expr_stmt|;
return|return
name|Standard
operator|.
name|start
argument_list|(
name|RootDocProcessor
operator|.
name|process
argument_list|(
name|root
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|int
name|optionLength
parameter_list|(
name|String
name|option
parameter_list|)
block|{
name|Integer
name|length
init|=
name|StabilityOptions
operator|.
name|optionLength
argument_list|(
name|option
argument_list|)
decl_stmt|;
if|if
condition|(
name|length
operator|!=
literal|null
condition|)
block|{
return|return
name|length
return|;
block|}
return|return
name|Standard
operator|.
name|optionLength
argument_list|(
name|option
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|validOptions
parameter_list|(
name|String
index|[]
index|[]
name|options
parameter_list|,
name|DocErrorReporter
name|reporter
parameter_list|)
block|{
name|StabilityOptions
operator|.
name|validOptions
argument_list|(
name|options
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
name|String
index|[]
index|[]
name|filteredOptions
init|=
name|StabilityOptions
operator|.
name|filterOptions
argument_list|(
name|options
argument_list|)
decl_stmt|;
return|return
name|Standard
operator|.
name|validOptions
argument_list|(
name|filteredOptions
argument_list|,
name|reporter
argument_list|)
return|;
block|}
block|}
end_class

end_unit

