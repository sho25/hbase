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
name|util
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
name|Field
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
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessController
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedAction
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|UnsafeAvailChecker
block|{
specifier|private
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
literal|"sun.misc.Unsafe"
decl_stmt|;
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
name|UnsafeAvailChecker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|avail
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|unaligned
init|=
literal|false
decl_stmt|;
static|static
block|{
name|avail
operator|=
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Boolean
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Boolean
name|run
parameter_list|()
block|{
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|Class
operator|.
name|forName
argument_list|(
name|CLASS_NAME
argument_list|)
decl_stmt|;
name|Field
name|f
init|=
name|clazz
operator|.
name|getDeclaredField
argument_list|(
literal|"theUnsafe"
argument_list|)
decl_stmt|;
name|f
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|f
operator|.
name|get
argument_list|(
literal|null
argument_list|)
operator|!=
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"sun.misc.Unsafe is not available/accessible"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// When Unsafe itself is not available/accessible consider unaligned as false.
if|if
condition|(
name|avail
condition|)
block|{
try|try
block|{
comment|// Using java.nio.Bits#unaligned() to check for unaligned-access capability
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|Class
operator|.
name|forName
argument_list|(
literal|"java.nio.Bits"
argument_list|)
decl_stmt|;
name|Method
name|m
init|=
name|clazz
operator|.
name|getDeclaredMethod
argument_list|(
literal|"unaligned"
argument_list|)
decl_stmt|;
name|m
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|unaligned
operator|=
operator|(
name|Boolean
operator|)
name|m
operator|.
name|invoke
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"java.nio.Bits#unaligned() check failed."
operator|+
literal|"Unsafe based read/write of primitive types won't be used"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @return true when running JVM is having sun's Unsafe package available in it and it is    *         accessible.    */
specifier|public
specifier|static
name|boolean
name|isAvailable
parameter_list|()
block|{
return|return
name|avail
return|;
block|}
comment|/**    * @return true when running JVM is having sun's Unsafe package available in it and underlying    *         system having unaligned-access capability.    */
specifier|public
specifier|static
name|boolean
name|unaligned
parameter_list|()
block|{
return|return
name|unaligned
return|;
block|}
specifier|private
name|UnsafeAvailChecker
parameter_list|()
block|{
comment|// private constructor to avoid instantiation
block|}
block|}
end_class

end_unit

