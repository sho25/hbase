begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|RuntimeMXBean
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
name|InvocationTargetException
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
name|nio
operator|.
name|ByteBuffer
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
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|JMException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanServer
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MalformedObjectNameException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|ObjectName
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Utilities for interacting with and monitoring DirectByteBuffer allocations.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|DirectMemoryUtils
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
name|DirectMemoryUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MEMORY_USED
init|=
literal|"MemoryUsed"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|MBeanServer
name|BEAN_SERVER
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ObjectName
name|NIO_DIRECT_POOL
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|HAS_MEMORY_USED_ATTRIBUTE
decl_stmt|;
static|static
block|{
comment|// initialize singletons. Only maintain a reference to the MBeanServer if
comment|// we're able to consume it -- hence convoluted logic.
name|ObjectName
name|n
init|=
literal|null
decl_stmt|;
name|MBeanServer
name|s
init|=
literal|null
decl_stmt|;
name|Object
name|a
init|=
literal|null
decl_stmt|;
try|try
block|{
name|n
operator|=
operator|new
name|ObjectName
argument_list|(
literal|"java.nio:type=BufferPool,name=direct"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MalformedObjectNameException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to initialize ObjectName for DirectByteBuffer allocations."
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|NIO_DIRECT_POOL
operator|=
name|n
expr_stmt|;
block|}
if|if
condition|(
name|NIO_DIRECT_POOL
operator|!=
literal|null
condition|)
block|{
name|s
operator|=
name|ManagementFactory
operator|.
name|getPlatformMBeanServer
argument_list|()
expr_stmt|;
block|}
name|BEAN_SERVER
operator|=
name|s
expr_stmt|;
if|if
condition|(
name|BEAN_SERVER
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|a
operator|=
name|BEAN_SERVER
operator|.
name|getAttribute
argument_list|(
name|NIO_DIRECT_POOL
argument_list|,
name|MEMORY_USED
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|JMException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Failed to retrieve nio.BufferPool direct MemoryUsed attribute: "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|HAS_MEMORY_USED_ATTRIBUTE
operator|=
name|a
operator|!=
literal|null
expr_stmt|;
block|}
comment|/**    * @return the setting of -XX:MaxDirectMemorySize as a long. Returns 0 if    *         -XX:MaxDirectMemorySize is not set.    */
specifier|public
specifier|static
name|long
name|getDirectMemorySize
parameter_list|()
block|{
name|RuntimeMXBean
name|runtimemxBean
init|=
name|ManagementFactory
operator|.
name|getRuntimeMXBean
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|arguments
init|=
name|runtimemxBean
operator|.
name|getInputArguments
argument_list|()
decl_stmt|;
name|long
name|multiplier
init|=
literal|1
decl_stmt|;
comment|//for the byte case.
for|for
control|(
name|String
name|s
range|:
name|arguments
control|)
block|{
if|if
condition|(
name|s
operator|.
name|contains
argument_list|(
literal|"-XX:MaxDirectMemorySize="
argument_list|)
condition|)
block|{
name|String
name|memSize
init|=
name|s
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
operator|.
name|replace
argument_list|(
literal|"-xx:maxdirectmemorysize="
argument_list|,
literal|""
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
name|memSize
operator|.
name|contains
argument_list|(
literal|"k"
argument_list|)
condition|)
block|{
name|multiplier
operator|=
literal|1024
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|memSize
operator|.
name|contains
argument_list|(
literal|"m"
argument_list|)
condition|)
block|{
name|multiplier
operator|=
literal|1048576
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|memSize
operator|.
name|contains
argument_list|(
literal|"g"
argument_list|)
condition|)
block|{
name|multiplier
operator|=
literal|1073741824
expr_stmt|;
block|}
name|memSize
operator|=
name|memSize
operator|.
name|replaceAll
argument_list|(
literal|"[^\\d]"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|long
name|retValue
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|memSize
argument_list|)
decl_stmt|;
return|return
name|retValue
operator|*
name|multiplier
return|;
block|}
block|}
return|return
literal|0
return|;
block|}
comment|/**    * @return the current amount of direct memory used.    */
specifier|public
specifier|static
name|long
name|getDirectMemoryUsage
parameter_list|()
block|{
if|if
condition|(
name|BEAN_SERVER
operator|==
literal|null
operator|||
name|NIO_DIRECT_POOL
operator|==
literal|null
operator|||
operator|!
name|HAS_MEMORY_USED_ATTRIBUTE
condition|)
return|return
literal|0
return|;
try|try
block|{
name|Long
name|value
init|=
operator|(
name|Long
operator|)
name|BEAN_SERVER
operator|.
name|getAttribute
argument_list|(
name|NIO_DIRECT_POOL
argument_list|,
name|MEMORY_USED
argument_list|)
decl_stmt|;
return|return
name|value
operator|==
literal|null
condition|?
literal|0
else|:
name|value
return|;
block|}
catch|catch
parameter_list|(
name|JMException
name|e
parameter_list|)
block|{
comment|// should print further diagnostic information?
return|return
literal|0
return|;
block|}
block|}
comment|/**    * DirectByteBuffers are garbage collected by using a phantom reference and a    * reference queue. Every once a while, the JVM checks the reference queue and    * cleans the DirectByteBuffers. However, as this doesn't happen    * immediately after discarding all references to a DirectByteBuffer, it's    * easy to OutOfMemoryError yourself using DirectByteBuffers. This function    * explicitly calls the Cleaner method of a DirectByteBuffer.    *     * @param toBeDestroyed    *          The DirectByteBuffer that will be "cleaned". Utilizes reflection.    *              */
specifier|public
specifier|static
name|void
name|destroyDirectByteBuffer
parameter_list|(
name|ByteBuffer
name|toBeDestroyed
parameter_list|)
throws|throws
name|IllegalArgumentException
throws|,
name|IllegalAccessException
throws|,
name|InvocationTargetException
throws|,
name|SecurityException
throws|,
name|NoSuchMethodException
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|toBeDestroyed
operator|.
name|isDirect
argument_list|()
argument_list|,
literal|"toBeDestroyed isn't direct!"
argument_list|)
expr_stmt|;
name|Method
name|cleanerMethod
init|=
name|toBeDestroyed
operator|.
name|getClass
argument_list|()
operator|.
name|getMethod
argument_list|(
literal|"cleaner"
argument_list|)
decl_stmt|;
name|cleanerMethod
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Object
name|cleaner
init|=
name|cleanerMethod
operator|.
name|invoke
argument_list|(
name|toBeDestroyed
argument_list|)
decl_stmt|;
name|Method
name|cleanMethod
init|=
name|cleaner
operator|.
name|getClass
argument_list|()
operator|.
name|getMethod
argument_list|(
literal|"clean"
argument_list|)
decl_stmt|;
name|cleanMethod
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|cleanMethod
operator|.
name|invoke
argument_list|(
name|cleaner
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

