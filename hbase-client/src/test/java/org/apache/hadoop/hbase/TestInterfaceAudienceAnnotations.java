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
name|lang
operator|.
name|annotation
operator|.
name|Annotation
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
name|Set
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|ClassFinder
operator|.
name|And
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
name|ClassFinder
operator|.
name|FileNameFilter
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
name|ClassFinder
operator|.
name|Not
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
name|ClassTestFinder
operator|.
name|TestClassFilter
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
name|ClassTestFinder
operator|.
name|TestFileNameFilter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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
comment|/**  * Test cases for ensuring our client visible classes have annotations  * for {@link InterfaceAudience}.  *  * All classes in hbase-client and hbase-common module MUST have InterfaceAudience  * annotations. All InterfaceAudience.Public annotated classes MUST also have InterfaceStability  * annotations. Think twice about marking an interface InterfaceAudience.Public. Make sure that  * it is an interface, not a class (for most cases), and clients will actually depend on it. Once  * something is marked with Public, we cannot change the signatures within the major release. NOT  * everything in the hbase-client module or every java public class has to be marked with  * InterfaceAudience.Public. ONLY the ones that an hbase application will directly use (Table, Get,  * etc, versus ProtobufUtil).  *  * Also note that HBase has it's own annotations in hbase-annotations module with the same names  * as in Hadoop. You should use the HBase's classes.  *  * See https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/InterfaceClassification.html  * and https://issues.apache.org/jira/browse/HBASE-10462.  */
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
name|TestInterfaceAudienceAnnotations
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestInterfaceAudienceAnnotations
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Selects classes with generated in their package name */
class|class
name|GeneratedClassFilter
implements|implements
name|ClassFinder
operator|.
name|ClassFilter
block|{
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
name|c
operator|.
name|getPackage
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|contains
argument_list|(
literal|"generated"
argument_list|)
return|;
block|}
block|}
comment|/** Selects classes with one of the {@link InterfaceAudience} annotation in their class    * declaration.    */
class|class
name|InterfaceAudienceAnnotatedClassFilter
implements|implements
name|ClassFinder
operator|.
name|ClassFilter
block|{
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
if|if
condition|(
name|getAnnotation
argument_list|(
name|c
argument_list|)
operator|!=
literal|null
condition|)
block|{
comment|// class itself has a declared annotation.
return|return
literal|true
return|;
block|}
comment|// If this is an internal class, look for the encapsulating class to see whether it has
comment|// annotation. All inner classes of private classes are considered annotated.
return|return
name|isAnnotatedPrivate
argument_list|(
name|c
operator|.
name|getEnclosingClass
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|isAnnotatedPrivate
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
name|c
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Class
argument_list|<
name|?
argument_list|>
name|ann
init|=
name|getAnnotation
argument_list|(
name|c
argument_list|)
decl_stmt|;
if|if
condition|(
name|ann
operator|!=
literal|null
operator|&&
operator|!
name|InterfaceAudience
operator|.
name|Public
operator|.
name|class
operator|.
name|equals
argument_list|(
name|ann
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
name|isAnnotatedPrivate
argument_list|(
name|c
operator|.
name|getEnclosingClass
argument_list|()
argument_list|)
return|;
block|}
specifier|protected
name|Class
argument_list|<
name|?
argument_list|>
name|getAnnotation
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
comment|// we should get only declared annotations, not inherited ones
name|Annotation
index|[]
name|anns
init|=
name|c
operator|.
name|getDeclaredAnnotations
argument_list|()
decl_stmt|;
for|for
control|(
name|Annotation
name|ann
range|:
name|anns
control|)
block|{
comment|// Hadoop clearly got it wrong for not making the annotation values (private, public, ..)
comment|// an enum instead we have three independent annotations!
name|Class
argument_list|<
name|?
argument_list|>
name|type
init|=
name|ann
operator|.
name|annotationType
argument_list|()
decl_stmt|;
if|if
condition|(
name|isInterfaceAudienceClass
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|type
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
comment|/** Selects classes with one of the {@link InterfaceStability} annotation in their class    * declaration.    */
class|class
name|InterfaceStabilityAnnotatedClassFilter
implements|implements
name|ClassFinder
operator|.
name|ClassFilter
block|{
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
if|if
condition|(
name|getAnnotation
argument_list|(
name|c
argument_list|)
operator|!=
literal|null
condition|)
block|{
comment|// class itself has a declared annotation.
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|protected
name|Class
argument_list|<
name|?
argument_list|>
name|getAnnotation
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
comment|// we should get only declared annotations, not inherited ones
name|Annotation
index|[]
name|anns
init|=
name|c
operator|.
name|getDeclaredAnnotations
argument_list|()
decl_stmt|;
for|for
control|(
name|Annotation
name|ann
range|:
name|anns
control|)
block|{
comment|// Hadoop clearly got it wrong for not making the annotation values (private, public, ..)
comment|// an enum instead we have three independent annotations!
name|Class
argument_list|<
name|?
argument_list|>
name|type
init|=
name|ann
operator|.
name|annotationType
argument_list|()
decl_stmt|;
if|if
condition|(
name|isInterfaceStabilityClass
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|type
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
comment|/** Selects classes with one of the {@link InterfaceAudience.Public} annotation in their    * class declaration.    */
class|class
name|InterfaceAudiencePublicAnnotatedClassFilter
extends|extends
name|InterfaceAudienceAnnotatedClassFilter
block|{
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
operator|(
name|InterfaceAudience
operator|.
name|Public
operator|.
name|class
operator|.
name|equals
argument_list|(
name|getAnnotation
argument_list|(
name|c
argument_list|)
argument_list|)
operator|)
return|;
block|}
block|}
comment|/**    * Selects InterfaceAudience or InterfaceStability classes. Don't go meta!!!    */
class|class
name|IsInterfaceStabilityClassFilter
implements|implements
name|ClassFinder
operator|.
name|ClassFilter
block|{
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
name|isInterfaceAudienceClass
argument_list|(
name|c
argument_list|)
operator|||
name|isInterfaceStabilityClass
argument_list|(
name|c
argument_list|)
return|;
block|}
block|}
specifier|private
name|boolean
name|isInterfaceAudienceClass
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
return|return
name|c
operator|.
name|equals
argument_list|(
name|InterfaceAudience
operator|.
name|Public
operator|.
name|class
argument_list|)
operator|||
name|c
operator|.
name|equals
argument_list|(
name|InterfaceAudience
operator|.
name|Private
operator|.
name|class
argument_list|)
operator|||
name|c
operator|.
name|equals
argument_list|(
name|InterfaceAudience
operator|.
name|LimitedPrivate
operator|.
name|class
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|isInterfaceStabilityClass
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
return|return
name|c
operator|.
name|equals
argument_list|(
name|InterfaceStability
operator|.
name|Stable
operator|.
name|class
argument_list|)
operator|||
name|c
operator|.
name|equals
argument_list|(
name|InterfaceStability
operator|.
name|Unstable
operator|.
name|class
argument_list|)
operator|||
name|c
operator|.
name|equals
argument_list|(
name|InterfaceStability
operator|.
name|Evolving
operator|.
name|class
argument_list|)
return|;
block|}
comment|/** Selects classes that are declared public */
class|class
name|PublicClassFilter
implements|implements
name|ClassFinder
operator|.
name|ClassFilter
block|{
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
name|int
name|mod
init|=
name|c
operator|.
name|getModifiers
argument_list|()
decl_stmt|;
return|return
name|Modifier
operator|.
name|isPublic
argument_list|(
name|mod
argument_list|)
return|;
block|}
block|}
comment|/** Selects paths (jars and class dirs) only from the main code, not test classes */
class|class
name|MainCodeResourcePathFilter
implements|implements
name|ClassFinder
operator|.
name|ResourcePathFilter
block|{
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
name|resourcePath
operator|.
name|contains
argument_list|(
literal|"test-classes"
argument_list|)
operator|&&
operator|!
name|resourcePath
operator|.
name|contains
argument_list|(
literal|"tests.jar"
argument_list|)
return|;
block|}
block|}
comment|/**    * Selects classes that appear to be source instrumentation from Clover.    * Clover generates instrumented code in order to calculate coverage. Part of the    * generated source is a static inner class on each source class.    *    * - has an enclosing class    * - enclosing class is not an interface    * - name starts with "__CLR"    */
class|class
name|CloverInstrumentationFilter
implements|implements
name|ClassFinder
operator|.
name|ClassFilter
block|{
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
name|clazz
parameter_list|)
block|{
name|boolean
name|clover
init|=
literal|false
decl_stmt|;
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|enclosing
init|=
name|clazz
operator|.
name|getEnclosingClass
argument_list|()
decl_stmt|;
if|if
condition|(
name|enclosing
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|enclosing
operator|.
name|isInterface
argument_list|()
operator|)
condition|)
block|{
name|clover
operator|=
name|clazz
operator|.
name|getSimpleName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"__CLR"
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|clover
return|;
block|}
block|}
comment|/**    * Checks whether all the classes in client and common modules contain    * {@link InterfaceAudience} annotations.    */
annotation|@
name|Test
specifier|public
name|void
name|testInterfaceAudienceAnnotation
parameter_list|()
throws|throws
name|ClassNotFoundException
throws|,
name|IOException
throws|,
name|LinkageError
block|{
comment|// find classes that are:
comment|// In the main jar
comment|// AND are public
comment|// NOT test classes
comment|// AND NOT generated classes
comment|// AND are NOT annotated with InterfaceAudience
comment|// AND are NOT from Clover rewriting sources
name|ClassFinder
name|classFinder
init|=
operator|new
name|ClassFinder
argument_list|(
operator|new
name|MainCodeResourcePathFilter
argument_list|()
argument_list|,
operator|new
name|Not
argument_list|(
operator|(
name|FileNameFilter
operator|)
operator|new
name|TestFileNameFilter
argument_list|()
argument_list|)
argument_list|,
operator|new
name|And
argument_list|(
operator|new
name|PublicClassFilter
argument_list|()
argument_list|,
operator|new
name|Not
argument_list|(
operator|new
name|TestClassFilter
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Not
argument_list|(
operator|new
name|GeneratedClassFilter
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Not
argument_list|(
operator|new
name|IsInterfaceStabilityClassFilter
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Not
argument_list|(
operator|new
name|InterfaceAudienceAnnotatedClassFilter
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Not
argument_list|(
operator|new
name|CloverInstrumentationFilter
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|classes
init|=
name|classFinder
operator|.
name|findClasses
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"These are the classes that DO NOT have @InterfaceAudience annotation:"
argument_list|)
expr_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
range|:
name|classes
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|clazz
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"All classes should have @InterfaceAudience annotation"
argument_list|,
literal|0
argument_list|,
name|classes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Checks whether all the classes in client and common modules that are marked    * InterfaceAudience.Public also have {@link InterfaceStability} annotations.    */
annotation|@
name|Test
specifier|public
name|void
name|testInterfaceStabilityAnnotation
parameter_list|()
throws|throws
name|ClassNotFoundException
throws|,
name|IOException
throws|,
name|LinkageError
block|{
comment|// find classes that are:
comment|// In the main jar
comment|// AND are public
comment|// NOT test classes
comment|// AND NOT generated classes
comment|// AND are annotated with InterfaceAudience.Public
comment|// AND NOT annotated with InterfaceStability
name|ClassFinder
name|classFinder
init|=
operator|new
name|ClassFinder
argument_list|(
operator|new
name|MainCodeResourcePathFilter
argument_list|()
argument_list|,
operator|new
name|Not
argument_list|(
operator|(
name|FileNameFilter
operator|)
operator|new
name|TestFileNameFilter
argument_list|()
argument_list|)
argument_list|,
operator|new
name|And
argument_list|(
operator|new
name|PublicClassFilter
argument_list|()
argument_list|,
operator|new
name|Not
argument_list|(
operator|new
name|TestClassFilter
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Not
argument_list|(
operator|new
name|GeneratedClassFilter
argument_list|()
argument_list|)
argument_list|,
operator|new
name|InterfaceAudiencePublicAnnotatedClassFilter
argument_list|()
argument_list|,
operator|new
name|Not
argument_list|(
operator|new
name|IsInterfaceStabilityClassFilter
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Not
argument_list|(
operator|new
name|InterfaceStabilityAnnotatedClassFilter
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|classes
init|=
name|classFinder
operator|.
name|findClasses
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"These are the classes that DO NOT have @InterfaceStability annotation:"
argument_list|)
expr_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
range|:
name|classes
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|clazz
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"All classes that are marked with @InterfaceAudience.Public should "
operator|+
literal|"have @InterfaceStability annotation as well"
argument_list|,
literal|0
argument_list|,
name|classes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

