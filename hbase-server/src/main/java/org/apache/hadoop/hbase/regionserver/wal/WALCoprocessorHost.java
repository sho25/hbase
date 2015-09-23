begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|wal
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
name|Coprocessor
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
name|HRegionInfo
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
name|coprocessor
operator|.
name|*
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
name|wal
operator|.
name|WAL
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
name|wal
operator|.
name|WALKey
import|;
end_import

begin_comment
comment|/**  * Implements the coprocessor environment and runtime support for coprocessors  * loaded within a {@link WAL}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|WALCoprocessorHost
extends|extends
name|CoprocessorHost
argument_list|<
name|WALCoprocessorHost
operator|.
name|WALEnvironment
argument_list|>
block|{
comment|/**    * Encapsulation of the environment of each coprocessor    */
specifier|static
class|class
name|WALEnvironment
extends|extends
name|CoprocessorHost
operator|.
name|Environment
implements|implements
name|WALCoprocessorEnvironment
block|{
specifier|private
specifier|final
name|WAL
name|wal
decl_stmt|;
specifier|final
name|boolean
name|useLegacyPre
decl_stmt|;
specifier|final
name|boolean
name|useLegacyPost
decl_stmt|;
annotation|@
name|Override
specifier|public
name|WAL
name|getWAL
parameter_list|()
block|{
return|return
name|wal
return|;
block|}
comment|/**      * Constructor      * @param implClass - not used      * @param impl the coprocessor instance      * @param priority chaining priority      * @param seq load sequence      * @param conf configuration      * @param wal WAL      */
specifier|public
name|WALEnvironment
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
parameter_list|,
specifier|final
name|Coprocessor
name|impl
parameter_list|,
specifier|final
name|int
name|priority
parameter_list|,
specifier|final
name|int
name|seq
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|WAL
name|wal
parameter_list|)
block|{
name|super
argument_list|(
name|impl
argument_list|,
name|priority
argument_list|,
name|seq
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|wal
operator|=
name|wal
expr_stmt|;
comment|// Pick which version of the API we'll call.
comment|// This way we avoid calling the new version on older WALObservers so
comment|// we can maintain binary compatibility.
comment|// See notes in javadoc for WALObserver
name|useLegacyPre
operator|=
name|useLegacyMethod
argument_list|(
name|impl
operator|.
name|getClass
argument_list|()
argument_list|,
literal|"preWALWrite"
argument_list|,
name|ObserverContext
operator|.
name|class
argument_list|,
name|HRegionInfo
operator|.
name|class
argument_list|,
name|WALKey
operator|.
name|class
argument_list|,
name|WALEdit
operator|.
name|class
argument_list|)
expr_stmt|;
name|useLegacyPost
operator|=
name|useLegacyMethod
argument_list|(
name|impl
operator|.
name|getClass
argument_list|()
argument_list|,
literal|"postWALWrite"
argument_list|,
name|ObserverContext
operator|.
name|class
argument_list|,
name|HRegionInfo
operator|.
name|class
argument_list|,
name|WALKey
operator|.
name|class
argument_list|,
name|WALEdit
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|final
name|WAL
name|wal
decl_stmt|;
comment|/**    * Constructor    * @param log the write ahead log    * @param conf the configuration    */
specifier|public
name|WALCoprocessorHost
parameter_list|(
specifier|final
name|WAL
name|log
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
comment|// We don't want to require an Abortable passed down through (FS)HLog, so
comment|// this means that a failure to load of a WAL coprocessor won't abort the
comment|// server. This isn't ideal, and means that security components that
comment|// utilize a WALObserver will have to check the observer initialization
comment|// state manually. However, WALObservers will eventually go away so it
comment|// should be an acceptable state of affairs.
name|super
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|wal
operator|=
name|log
expr_stmt|;
comment|// load system default cp's from configuration.
name|loadSystemCoprocessors
argument_list|(
name|conf
argument_list|,
name|WAL_COPROCESSOR_CONF_KEY
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|WALEnvironment
name|createEnvironment
parameter_list|(
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
parameter_list|,
specifier|final
name|Coprocessor
name|instance
parameter_list|,
specifier|final
name|int
name|priority
parameter_list|,
specifier|final
name|int
name|seq
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|new
name|WALEnvironment
argument_list|(
name|implClass
argument_list|,
name|instance
argument_list|,
name|priority
argument_list|,
name|seq
argument_list|,
name|conf
argument_list|,
name|this
operator|.
name|wal
argument_list|)
return|;
block|}
comment|/**    * @param info    * @param logKey    * @param logEdit    * @return true if default behavior should be bypassed, false otherwise    * @throws IOException    */
specifier|public
name|boolean
name|preWALWrite
parameter_list|(
specifier|final
name|HRegionInfo
name|info
parameter_list|,
specifier|final
name|WALKey
name|logKey
parameter_list|,
specifier|final
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|bypass
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|coprocessors
operator|==
literal|null
operator|||
name|this
operator|.
name|coprocessors
operator|.
name|isEmpty
argument_list|()
condition|)
return|return
name|bypass
return|;
name|ObserverContext
argument_list|<
name|WALCoprocessorEnvironment
argument_list|>
name|ctx
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|WALEnvironment
argument_list|>
name|envs
init|=
name|coprocessors
operator|.
name|get
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|envs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|WALEnvironment
name|env
init|=
name|envs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|WALObserver
condition|)
block|{
specifier|final
name|WALObserver
name|observer
init|=
operator|(
name|WALObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|ctx
operator|=
name|ObserverContext
operator|.
name|createAndPrepare
argument_list|(
name|env
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|Thread
name|currentThread
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
decl_stmt|;
name|ClassLoader
name|cl
init|=
name|currentThread
operator|.
name|getContextClassLoader
argument_list|()
decl_stmt|;
try|try
block|{
name|currentThread
operator|.
name|setContextClassLoader
argument_list|(
name|env
operator|.
name|getClassLoader
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|useLegacyPre
condition|)
block|{
if|if
condition|(
name|logKey
operator|instanceof
name|HLogKey
condition|)
block|{
name|observer
operator|.
name|preWALWrite
argument_list|(
name|ctx
argument_list|,
name|info
argument_list|,
operator|(
name|HLogKey
operator|)
name|logKey
argument_list|,
name|logEdit
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|legacyWarning
argument_list|(
name|observer
operator|.
name|getClass
argument_list|()
argument_list|,
literal|"There are wal keys present that are not HLogKey."
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|observer
operator|.
name|preWALWrite
argument_list|(
name|ctx
argument_list|,
name|info
argument_list|,
name|logKey
argument_list|,
name|logEdit
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|handleCoprocessorThrowable
argument_list|(
name|env
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|currentThread
operator|.
name|setContextClassLoader
argument_list|(
name|cl
argument_list|)
expr_stmt|;
block|}
name|bypass
operator||=
name|ctx
operator|.
name|shouldBypass
argument_list|()
expr_stmt|;
if|if
condition|(
name|ctx
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
return|return
name|bypass
return|;
block|}
comment|/**    * @param info    * @param logKey    * @param logEdit    * @throws IOException    */
specifier|public
name|void
name|postWALWrite
parameter_list|(
specifier|final
name|HRegionInfo
name|info
parameter_list|,
specifier|final
name|WALKey
name|logKey
parameter_list|,
specifier|final
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|coprocessors
operator|==
literal|null
operator|||
name|this
operator|.
name|coprocessors
operator|.
name|isEmpty
argument_list|()
condition|)
return|return;
name|ObserverContext
argument_list|<
name|WALCoprocessorEnvironment
argument_list|>
name|ctx
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|WALEnvironment
argument_list|>
name|envs
init|=
name|coprocessors
operator|.
name|get
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|envs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|WALEnvironment
name|env
init|=
name|envs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|WALObserver
condition|)
block|{
specifier|final
name|WALObserver
name|observer
init|=
operator|(
name|WALObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|ctx
operator|=
name|ObserverContext
operator|.
name|createAndPrepare
argument_list|(
name|env
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|Thread
name|currentThread
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
decl_stmt|;
name|ClassLoader
name|cl
init|=
name|currentThread
operator|.
name|getContextClassLoader
argument_list|()
decl_stmt|;
try|try
block|{
name|currentThread
operator|.
name|setContextClassLoader
argument_list|(
name|env
operator|.
name|getClassLoader
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|useLegacyPost
condition|)
block|{
if|if
condition|(
name|logKey
operator|instanceof
name|HLogKey
condition|)
block|{
name|observer
operator|.
name|postWALWrite
argument_list|(
name|ctx
argument_list|,
name|info
argument_list|,
operator|(
name|HLogKey
operator|)
name|logKey
argument_list|,
name|logEdit
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|legacyWarning
argument_list|(
name|observer
operator|.
name|getClass
argument_list|()
argument_list|,
literal|"There are wal keys present that are not HLogKey."
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|observer
operator|.
name|postWALWrite
argument_list|(
name|ctx
argument_list|,
name|info
argument_list|,
name|logKey
argument_list|,
name|logEdit
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|handleCoprocessorThrowable
argument_list|(
name|env
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|currentThread
operator|.
name|setContextClassLoader
argument_list|(
name|cl
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ctx
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

