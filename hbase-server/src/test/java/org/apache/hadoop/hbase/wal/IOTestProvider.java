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
name|wal
package|;
end_package

begin_import
import|import static
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
name|AbstractFSWALProvider
operator|.
name|DEFAULT_PROVIDER_ID
import|;
end_import

begin_import
import|import static
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
name|AbstractFSWALProvider
operator|.
name|META_WAL_PROVIDER_ID
import|;
end_import

begin_import
import|import static
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
name|AbstractFSWALProvider
operator|.
name|WAL_FILE_NAME_DELIMITER
import|;
end_import

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
name|Collection
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|HConstants
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

begin_comment
comment|// imports for things that haven't moved from regionserver.wal yet.
end_comment

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
name|regionserver
operator|.
name|wal
operator|.
name|FSHLog
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
name|regionserver
operator|.
name|wal
operator|.
name|ProtobufLogWriter
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
name|regionserver
operator|.
name|wal
operator|.
name|WALActionsListener
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
name|FSUtils
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
operator|.
name|Entry
import|;
end_import

begin_comment
comment|/**  * A WAL Provider that returns a single thread safe WAL that optionally can skip parts of our  * normal interactions with HDFS.  *  * This implementation picks a directory in HDFS based on the same mechanisms as the   * {@link FSHLogProvider}. Users can configure how much interaction  * we have with HDFS with the configuration property "hbase.wal.iotestprovider.operations".  * The value should be a comma separated list of allowed operations:  *<ul>  *<li><em>append</em>   : edits will be written to the underlying filesystem  *<li><em>sync</em>     : wal syncs will result in hflush calls  *<li><em>fileroll</em> : roll requests will result in creating a new file on the underlying  *                           filesystem.  *</ul>  * Additionally, the special cases "all" and "none" are recognized.  * If ommited, the value defaults to "all."  * Behavior is undefined if "all" or "none" are paired with additional values. Behavior is also  * undefined if values not listed above are included.  *  * Only those operations listed will occur between the returned WAL and HDFS. All others  * will be no-ops.  *  * Note that in the case of allowing "append" operations but not allowing "fileroll", the returned  * WAL will just keep writing to the same file. This won't avoid all costs associated with file  * management over time, becaue the data set size may result in additional HDFS block allocations.  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|IOTestProvider
implements|implements
name|WALProvider
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
name|IOTestProvider
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ALLOWED_OPERATIONS
init|=
literal|"hbase.wal.iotestprovider.operations"
decl_stmt|;
specifier|private
enum|enum
name|AllowedOperations
block|{
name|all
block|,
name|append
block|,
name|sync
block|,
name|fileroll
block|,
name|none
block|;   }
specifier|private
name|FSHLog
name|log
init|=
literal|null
decl_stmt|;
comment|/**    * @param factory factory that made us, identity used for FS layout. may not be null    * @param conf may not be null    * @param listeners may be null    * @param providerId differentiate between providers from one facotry, used for FS layout. may be    *                   null    */
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
specifier|final
name|WALFactory
name|factory
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
parameter_list|,
name|String
name|providerId
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
literal|null
operator|!=
name|log
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"WALProvider.init should only be called once."
argument_list|)
throw|;
block|}
if|if
condition|(
literal|null
operator|==
name|providerId
condition|)
block|{
name|providerId
operator|=
name|DEFAULT_PROVIDER_ID
expr_stmt|;
block|}
specifier|final
name|String
name|logPrefix
init|=
name|factory
operator|.
name|factoryId
operator|+
name|WAL_FILE_NAME_DELIMITER
operator|+
name|providerId
decl_stmt|;
name|log
operator|=
operator|new
name|IOTestWAL
argument_list|(
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getWALDirectoryName
argument_list|(
name|factory
operator|.
name|factoryId
argument_list|)
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|,
name|conf
argument_list|,
name|listeners
argument_list|,
literal|true
argument_list|,
name|logPrefix
argument_list|,
name|META_WAL_PROVIDER_ID
operator|.
name|equals
argument_list|(
name|providerId
argument_list|)
condition|?
name|META_WAL_PROVIDER_ID
else|:
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|WAL
argument_list|>
name|getWALs
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|WAL
argument_list|>
name|wals
init|=
operator|new
name|ArrayList
argument_list|<
name|WAL
argument_list|>
argument_list|()
decl_stmt|;
name|wals
operator|.
name|add
argument_list|(
name|log
argument_list|)
expr_stmt|;
return|return
name|wals
return|;
block|}
annotation|@
name|Override
specifier|public
name|WAL
name|getWAL
parameter_list|(
specifier|final
name|byte
index|[]
name|identifier
parameter_list|,
name|byte
index|[]
name|namespace
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|log
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|log
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
block|{
name|log
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|IOTestWAL
extends|extends
name|FSHLog
block|{
specifier|private
specifier|final
name|boolean
name|doFileRolls
decl_stmt|;
comment|// Used to differntiate between roll calls before and after we finish construction.
specifier|private
specifier|final
name|boolean
name|initialized
decl_stmt|;
comment|/**      * Create an edit log at the given<code>dir</code> location.      *      * You should never have to load an existing log. If there is a log at      * startup, it should have already been processed and deleted by the time the      * WAL object is started up.      *      * @param fs filesystem handle      * @param rootDir path to where logs and oldlogs      * @param logDir dir where wals are stored      * @param archiveDir dir where wals are archived      * @param conf configuration to use      * @param listeners Listeners on WAL events. Listeners passed here will      * be registered before we do anything else; e.g. the      * Constructor {@link #rollWriter()}.      * @param failIfWALExists If true IOException will be thrown if files related to this wal      *        already exist.      * @param prefix should always be hostname and port in distributed env and      *        it will be URL encoded before being used.      *        If prefix is null, "wal" will be used      * @param suffix will be url encoded. null is treated as empty. non-empty must start with      *        {@link AbstractFSWALProvider#WAL_FILE_NAME_DELIMITER}      * @throws IOException      */
specifier|public
name|IOTestWAL
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|rootDir
parameter_list|,
specifier|final
name|String
name|logDir
parameter_list|,
specifier|final
name|String
name|archiveDir
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
parameter_list|,
specifier|final
name|boolean
name|failIfWALExists
parameter_list|,
specifier|final
name|String
name|prefix
parameter_list|,
specifier|final
name|String
name|suffix
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|logDir
argument_list|,
name|archiveDir
argument_list|,
name|conf
argument_list|,
name|listeners
argument_list|,
name|failIfWALExists
argument_list|,
name|prefix
argument_list|,
name|suffix
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|operations
init|=
name|conf
operator|.
name|getStringCollection
argument_list|(
name|ALLOWED_OPERATIONS
argument_list|)
decl_stmt|;
name|doFileRolls
operator|=
name|operations
operator|.
name|isEmpty
argument_list|()
operator|||
name|operations
operator|.
name|contains
argument_list|(
name|AllowedOperations
operator|.
name|all
operator|.
name|name
argument_list|()
argument_list|)
operator|||
name|operations
operator|.
name|contains
argument_list|(
name|AllowedOperations
operator|.
name|fileroll
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|initialized
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Initialized with file rolling "
operator|+
operator|(
name|doFileRolls
condition|?
literal|"enabled"
else|:
literal|"disabled"
operator|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Writer
name|noRollsWriter
decl_stmt|;
comment|// creatWriterInstance is where the new pipeline is set up for doing file rolls
comment|// if we are skipping it, just keep returning the same writer.
annotation|@
name|Override
specifier|protected
name|Writer
name|createWriterInstance
parameter_list|(
specifier|final
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
comment|// we get called from the FSHLog constructor (!); always roll in this case since
comment|// we don't know yet if we're supposed to generally roll and
comment|// we need an initial file in the case of doing appends but no rolls.
if|if
condition|(
operator|!
name|initialized
operator|||
name|doFileRolls
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"creating new writer instance."
argument_list|)
expr_stmt|;
specifier|final
name|ProtobufLogWriter
name|writer
init|=
operator|new
name|IOTestWriter
argument_list|()
decl_stmt|;
name|writer
operator|.
name|init
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|initialized
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"storing initial writer instance in case file rolling isn't allowed."
argument_list|)
expr_stmt|;
name|noRollsWriter
operator|=
name|writer
expr_stmt|;
block|}
return|return
name|writer
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"WAL rolling disabled, returning the first writer."
argument_list|)
expr_stmt|;
comment|// Initial assignment happens during the constructor call, so there ought not be
comment|// a race for first assignment.
return|return
name|noRollsWriter
return|;
block|}
block|}
block|}
comment|/**    * Presumes init will be called by a single thread prior to any access of other methods.    */
specifier|private
specifier|static
class|class
name|IOTestWriter
extends|extends
name|ProtobufLogWriter
block|{
specifier|private
name|boolean
name|doAppends
decl_stmt|;
specifier|private
name|boolean
name|doSyncs
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|boolean
name|overwritable
parameter_list|)
throws|throws
name|IOException
block|{
name|Collection
argument_list|<
name|String
argument_list|>
name|operations
init|=
name|conf
operator|.
name|getStringCollection
argument_list|(
name|ALLOWED_OPERATIONS
argument_list|)
decl_stmt|;
if|if
condition|(
name|operations
operator|.
name|isEmpty
argument_list|()
operator|||
name|operations
operator|.
name|contains
argument_list|(
name|AllowedOperations
operator|.
name|all
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
name|doAppends
operator|=
name|doSyncs
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|operations
operator|.
name|contains
argument_list|(
name|AllowedOperations
operator|.
name|none
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
name|doAppends
operator|=
name|doSyncs
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|doAppends
operator|=
name|operations
operator|.
name|contains
argument_list|(
name|AllowedOperations
operator|.
name|append
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|doSyncs
operator|=
name|operations
operator|.
name|contains
argument_list|(
name|AllowedOperations
operator|.
name|sync
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"IOTestWriter initialized with appends "
operator|+
operator|(
name|doAppends
condition|?
literal|"enabled"
else|:
literal|"disabled"
operator|)
operator|+
literal|" and syncs "
operator|+
operator|(
name|doSyncs
condition|?
literal|"enabled"
else|:
literal|"disabled"
operator|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|init
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|,
name|overwritable
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|getWriterClassName
parameter_list|()
block|{
return|return
name|ProtobufLogWriter
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
name|Entry
name|entry
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|doAppends
condition|)
block|{
name|super
operator|.
name|append
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|sync
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|doSyncs
condition|)
block|{
name|super
operator|.
name|sync
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumLogFiles
parameter_list|()
block|{
return|return
name|this
operator|.
name|log
operator|.
name|getNumLogFiles
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLogFileSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|log
operator|.
name|getLogFileSize
argument_list|()
return|;
block|}
block|}
end_class

end_unit

