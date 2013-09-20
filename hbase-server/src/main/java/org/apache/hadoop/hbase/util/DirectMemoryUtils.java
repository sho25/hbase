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
name|org
operator|.
name|apache
operator|.
name|hadoop
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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_import
import|import
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
comment|/**    * @return the setting of -XX:MaxDirectMemorySize as a long. Returns 0 if    *         -XX:MaxDirectMemorySize is not set.    */
specifier|public
specifier|static
name|long
name|getDirectMemorySize
parameter_list|()
block|{
name|RuntimeMXBean
name|RuntimemxBean
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
name|RuntimemxBean
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
argument_list|()
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

