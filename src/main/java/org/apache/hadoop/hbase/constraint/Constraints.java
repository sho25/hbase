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
name|constraint
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|HTableDescriptor
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
name|ImmutableBytesWritable
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
name|Bytes
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
name|Pair
import|;
end_import

begin_comment
comment|/**  * Utilities for adding/removing constraints from a table.  *<p>  * Constraints can be added on table load time, via the {@link HTableDescriptor}.  *<p>  * NOTE: this class is NOT thread safe. Concurrent setting/enabling/disabling of  * constraints can cause constraints to be run at incorrect times or not at all.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|Constraints
block|{
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_PRIORITY
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|Constraints
parameter_list|()
block|{   }
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
name|Constraints
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CONSTRAINT_HTD_KEY_PREFIX
init|=
literal|"constraint $"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Pattern
name|CONSTRAINT_HTD_ATTR_KEY_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
name|CONSTRAINT_HTD_KEY_PREFIX
argument_list|,
name|Pattern
operator|.
name|LITERAL
argument_list|)
decl_stmt|;
comment|// configuration key for if the constraint is enabled
specifier|private
specifier|static
specifier|final
name|String
name|ENABLED_KEY
init|=
literal|"_ENABLED"
decl_stmt|;
comment|// configuration key for the priority
specifier|private
specifier|static
specifier|final
name|String
name|PRIORITY_KEY
init|=
literal|"_PRIORITY"
decl_stmt|;
comment|// smallest priority a constraiNt can have
specifier|private
specifier|static
specifier|final
name|long
name|MIN_PRIORITY
init|=
literal|0L
decl_stmt|;
comment|// ensure a priority less than the smallest we could intentionally set
specifier|private
specifier|static
specifier|final
name|long
name|UNSET_PRIORITY
init|=
name|MIN_PRIORITY
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|static
name|String
name|COUNTER_KEY
init|=
literal|"hbase.constraint.counter"
decl_stmt|;
comment|/**    * Enable constraints on a table.    *<p>    * Currently, if you attempt to add a constraint to the table, then    * Constraints will automatically be turned on.    *     * @param desc    *          table description to add the processor    * @throws IOException    *           If the {@link ConstraintProcessor} CP couldn't be added to the    *           table.    */
specifier|public
specifier|static
name|void
name|enable
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|)
throws|throws
name|IOException
block|{
comment|// if the CP has already been loaded, do nothing
name|String
name|clazz
init|=
name|ConstraintProcessor
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|desc
operator|.
name|hasCoprocessor
argument_list|(
name|clazz
argument_list|)
condition|)
block|{
return|return;
block|}
comment|// add the constrain processor CP to the table
name|desc
operator|.
name|addCoprocessor
argument_list|(
name|clazz
argument_list|)
expr_stmt|;
block|}
comment|/**    * Turn off processing constraints for a given table, even if constraints have    * been turned on or added.    *     * @param desc    *          {@link HTableDescriptor} where to disable {@link Constraint    *          Constraints}.    */
specifier|public
specifier|static
name|void
name|disable
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|)
block|{
name|desc
operator|.
name|removeCoprocessor
argument_list|(
name|ConstraintProcessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Remove all {@link Constraint Constraints} that have been added to the table    * and turn off the constraint processing.    *<p>    * All {@link Configuration Configurations} and their associated    * {@link Constraint} are removed.    *     * @param desc    *          {@link HTableDescriptor} to remove {@link Constraint Constraints}    *          from.    */
specifier|public
specifier|static
name|void
name|remove
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|)
block|{
comment|// disable constraints
name|disable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
comment|// remove all the constraint settings
name|List
argument_list|<
name|ImmutableBytesWritable
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<
name|ImmutableBytesWritable
argument_list|>
argument_list|()
decl_stmt|;
comment|// loop through all the key, values looking for constraints
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|ImmutableBytesWritable
argument_list|>
name|e
range|:
name|desc
operator|.
name|getValues
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|key
init|=
name|Bytes
operator|.
name|toString
argument_list|(
operator|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|get
argument_list|()
operator|)
argument_list|)
decl_stmt|;
name|String
index|[]
name|className
init|=
name|CONSTRAINT_HTD_ATTR_KEY_PATTERN
operator|.
name|split
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|className
operator|.
name|length
operator|==
literal|2
condition|)
block|{
name|keys
operator|.
name|add
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// now remove all the keys we found
for|for
control|(
name|ImmutableBytesWritable
name|key
range|:
name|keys
control|)
block|{
name|desc
operator|.
name|remove
argument_list|(
name|key
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Check to see if the Constraint is currently set.    *     * @param desc    *          {@link HTableDescriptor} to check    * @param clazz    *          {@link Constraint} class to check for.    * @return<tt>true</tt> if the {@link Constraint} is present, even if it is    *         disabled.<tt>false</tt> otherwise.    */
specifier|public
specifier|static
name|boolean
name|has
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|clazz
parameter_list|)
block|{
return|return
name|getKeyValueForClass
argument_list|(
name|desc
argument_list|,
name|clazz
argument_list|)
operator|!=
literal|null
return|;
block|}
comment|/**    * Get the kv {@link Entry} in the descriptor for the specified class    *     * @param desc    *          {@link HTableDescriptor} to read    * @param clazz    *          to search for    * @return the {@link Pair} of<key, value> in the table, if that class is    *         present.<tt>null</tt> otherwise.    */
specifier|private
specifier|static
name|Pair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getKeyValueForClass
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|clazz
parameter_list|)
block|{
comment|// get the serialized version of the constraint
name|String
name|key
init|=
name|serializeConstraintClass
argument_list|(
name|clazz
argument_list|)
decl_stmt|;
name|String
name|value
init|=
name|desc
operator|.
name|getValue
argument_list|(
name|key
argument_list|)
decl_stmt|;
return|return
name|value
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|Pair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
comment|/**    * Add configuration-less constraints to the table.    *<p>    * This will overwrite any configuration associated with the previous    * constraint of the same class.    *<p>    * Each constraint, when added to the table, will have a specific priority,    * dictating the order in which the {@link Constraint} will be run. A    * {@link Constraint} earlier in the list will be run before those later in    * the list. The same logic applies between two Constraints over time (earlier    * added is run first on the regionserver).    *     * @param desc    *          {@link HTableDescriptor} to add {@link Constraint Constraints}    * @param constraints    *          {@link Constraint Constraints} to add. All constraints are    *          considered automatically enabled on add    * @throws IOException    *           If constraint could not be serialized/added to table    */
specifier|public
specifier|static
name|void
name|add
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
modifier|...
name|constraints
parameter_list|)
throws|throws
name|IOException
block|{
comment|// make sure constraints are enabled
name|enable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|long
name|priority
init|=
name|getNextPriority
argument_list|(
name|desc
argument_list|)
decl_stmt|;
comment|// store each constraint
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|clazz
range|:
name|constraints
control|)
block|{
name|addConstraint
argument_list|(
name|desc
argument_list|,
name|clazz
argument_list|,
literal|null
argument_list|,
name|priority
operator|++
argument_list|)
expr_stmt|;
block|}
name|updateLatestPriority
argument_list|(
name|desc
argument_list|,
name|priority
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add constraints and their associated configurations to the table.    *<p>    * Adding the same constraint class twice will overwrite the first    * constraint's configuration    *<p>    * Each constraint, when added to the table, will have a specific priority,    * dictating the order in which the {@link Constraint} will be run. A    * {@link Constraint} earlier in the list will be run before those later in    * the list. The same logic applies between two Constraints over time (earlier    * added is run first on the regionserver).    *     * @param desc    *          {@link HTableDescriptor} to add a {@link Constraint}    * @param constraints    *          {@link Pair} of a {@link Constraint} and its associated    *          {@link Configuration}. The Constraint will be configured on load    *          with the specified configuration.All constraints are considered    *          automatically enabled on add    * @throws IOException    *           if any constraint could not be deserialized. Assumes if 1    *           constraint is not loaded properly, something has gone terribly    *           wrong and that all constraints need to be enforced.    */
specifier|public
specifier|static
name|void
name|add
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|Pair
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
argument_list|,
name|Configuration
argument_list|>
modifier|...
name|constraints
parameter_list|)
throws|throws
name|IOException
block|{
name|enable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|long
name|priority
init|=
name|getNextPriority
argument_list|(
name|desc
argument_list|)
decl_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
argument_list|,
name|Configuration
argument_list|>
name|pair
range|:
name|constraints
control|)
block|{
name|addConstraint
argument_list|(
name|desc
argument_list|,
name|pair
operator|.
name|getFirst
argument_list|()
argument_list|,
name|pair
operator|.
name|getSecond
argument_list|()
argument_list|,
name|priority
operator|++
argument_list|)
expr_stmt|;
block|}
name|updateLatestPriority
argument_list|(
name|desc
argument_list|,
name|priority
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add a {@link Constraint} to the table with the given configuration    *<p>    * Each constraint, when added to the table, will have a specific priority,    * dictating the order in which the {@link Constraint} will be run. A    * {@link Constraint} added will run on the regionserver before those added to    * the {@link HTableDescriptor} later.    *     * @param desc    *          table descriptor to the constraint to    * @param constraint    *          to be added    * @param conf    *          configuration associated with the constraint    * @throws IOException    *           if any constraint could not be deserialized. Assumes if 1    *           constraint is not loaded properly, something has gone terribly    *           wrong and that all constraints need to be enforced.    */
specifier|public
specifier|static
name|void
name|add
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|constraint
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|enable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|long
name|priority
init|=
name|getNextPriority
argument_list|(
name|desc
argument_list|)
decl_stmt|;
name|addConstraint
argument_list|(
name|desc
argument_list|,
name|constraint
argument_list|,
name|conf
argument_list|,
name|priority
operator|++
argument_list|)
expr_stmt|;
name|updateLatestPriority
argument_list|(
name|desc
argument_list|,
name|priority
argument_list|)
expr_stmt|;
block|}
comment|/**    * Write the raw constraint and configuration to the descriptor.    *<p>    * This method takes care of creating a new configuration based on the passed    * in configuration and then updating that with enabled and priority of the    * constraint.    *<p>    * When a constraint is added, it is automatically enabled.    */
specifier|private
specifier|static
name|void
name|addConstraint
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|clazz
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|long
name|priority
parameter_list|)
throws|throws
name|IOException
block|{
name|writeConstraint
argument_list|(
name|desc
argument_list|,
name|serializeConstraintClass
argument_list|(
name|clazz
argument_list|)
argument_list|,
name|configure
argument_list|(
name|conf
argument_list|,
literal|true
argument_list|,
name|priority
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Setup the configuration for a constraint as to whether it is enabled and    * its priority    *     * @param conf    *          on which to base the new configuration    * @param enabled    *<tt>true</tt> if it should be run    * @param priority    *          relative to other constraints    * @returns a new configuration, storable in the {@link HTableDescriptor}    */
specifier|private
specifier|static
name|Configuration
name|configure
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|boolean
name|enabled
parameter_list|,
name|long
name|priority
parameter_list|)
block|{
comment|// create the configuration to actually be stored
comment|// clone if possible, but otherwise just create an empty configuration
name|Configuration
name|toWrite
init|=
name|conf
operator|==
literal|null
condition|?
operator|new
name|Configuration
argument_list|()
else|:
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// update internal properties
name|toWrite
operator|.
name|setBooleanIfUnset
argument_list|(
name|ENABLED_KEY
argument_list|,
name|enabled
argument_list|)
expr_stmt|;
comment|// set if unset long
if|if
condition|(
name|toWrite
operator|.
name|getLong
argument_list|(
name|PRIORITY_KEY
argument_list|,
name|UNSET_PRIORITY
argument_list|)
operator|==
name|UNSET_PRIORITY
condition|)
block|{
name|toWrite
operator|.
name|setLong
argument_list|(
name|PRIORITY_KEY
argument_list|,
name|priority
argument_list|)
expr_stmt|;
block|}
return|return
name|toWrite
return|;
block|}
comment|/**    * Just write the class to a String representation of the class as a key for    * the {@link HTableDescriptor}    *     * @param clazz    *          Constraint class to convert to a {@link HTableDescriptor} key    * @return key to store in the {@link HTableDescriptor}    */
specifier|private
specifier|static
name|String
name|serializeConstraintClass
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|clazz
parameter_list|)
block|{
name|String
name|constraintClazz
init|=
name|clazz
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
name|CONSTRAINT_HTD_KEY_PREFIX
operator|+
name|constraintClazz
return|;
block|}
comment|/**    * Write the given key and associated configuration to the    * {@link HTableDescriptor}    */
specifier|private
specifier|static
name|void
name|writeConstraint
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|String
name|key
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// store the key and conf in the descriptor
name|desc
operator|.
name|setValue
argument_list|(
name|key
argument_list|,
name|serializeConfiguration
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Write the configuration to a String    *     * @param conf    *          to write    * @return String representation of that configuration    * @throws IOException    */
specifier|private
specifier|static
name|String
name|serializeConfiguration
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// write the configuration out to the data stream
name|ByteArrayOutputStream
name|bos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|dos
init|=
operator|new
name|DataOutputStream
argument_list|(
name|bos
argument_list|)
decl_stmt|;
name|conf
operator|.
name|writeXml
argument_list|(
name|dos
argument_list|)
expr_stmt|;
name|dos
operator|.
name|flush
argument_list|()
expr_stmt|;
name|byte
index|[]
name|data
init|=
name|bos
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
return|return
name|Bytes
operator|.
name|toString
argument_list|(
name|data
argument_list|)
return|;
block|}
comment|/**    * Read the {@link Configuration} stored in the byte stream.    *     * @param bytes    *          to read from    * @return A valid configuration    */
specifier|private
specifier|static
name|Configuration
name|readConfiguration
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteArrayInputStream
name|is
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|conf
operator|.
name|addResource
argument_list|(
name|is
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
comment|/**    * Read in the configuration from the String encoded configuration    *     * @param bytes    *          to read from    * @return A valid configuration    * @throws IOException    *           if the configuration could not be read    */
specifier|private
specifier|static
name|Configuration
name|readConfiguration
parameter_list|(
name|String
name|bytes
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|readConfiguration
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|bytes
argument_list|)
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|long
name|getNextPriority
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|)
block|{
name|String
name|value
init|=
name|desc
operator|.
name|getValue
argument_list|(
name|COUNTER_KEY
argument_list|)
decl_stmt|;
name|long
name|priority
decl_stmt|;
comment|// get the current priority
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|priority
operator|=
name|MIN_PRIORITY
expr_stmt|;
block|}
else|else
block|{
name|priority
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
operator|+
literal|1
expr_stmt|;
block|}
return|return
name|priority
return|;
block|}
specifier|private
specifier|static
name|void
name|updateLatestPriority
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|long
name|priority
parameter_list|)
block|{
comment|// update the max priority
name|desc
operator|.
name|setValue
argument_list|(
name|COUNTER_KEY
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|priority
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Update the configuration for the {@link Constraint}; does not change the    * order in which the constraint is run.    *     * @param desc    *          {@link HTableDescriptor} to update    * @param clazz    *          {@link Constraint} to update    * @param configuration    *          to update the {@link Constraint} with.    * @throws IOException    *           if the Constraint was not stored correctly    * @throws IllegalArgumentException    *           if the Constraint was not present on this table.    */
specifier|public
specifier|static
name|void
name|setConfiguration
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|clazz
parameter_list|,
name|Configuration
name|configuration
parameter_list|)
throws|throws
name|IOException
throws|,
name|IllegalArgumentException
block|{
comment|// get the entry for this class
name|Pair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|e
init|=
name|getKeyValueForClass
argument_list|(
name|desc
argument_list|,
name|clazz
argument_list|)
decl_stmt|;
if|if
condition|(
name|e
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Constraint: "
operator|+
name|clazz
operator|.
name|getName
argument_list|()
operator|+
literal|" is not associated with this table."
argument_list|)
throw|;
block|}
comment|// clone over the configuration elements
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
comment|// read in the previous info about the constraint
name|Configuration
name|internal
init|=
name|readConfiguration
argument_list|(
name|e
operator|.
name|getSecond
argument_list|()
argument_list|)
decl_stmt|;
comment|// update the fields based on the previous settings
name|conf
operator|.
name|setIfUnset
argument_list|(
name|ENABLED_KEY
argument_list|,
name|internal
operator|.
name|get
argument_list|(
name|ENABLED_KEY
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setIfUnset
argument_list|(
name|PRIORITY_KEY
argument_list|,
name|internal
operator|.
name|get
argument_list|(
name|PRIORITY_KEY
argument_list|)
argument_list|)
expr_stmt|;
comment|// update the current value
name|writeConstraint
argument_list|(
name|desc
argument_list|,
name|e
operator|.
name|getFirst
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Remove the constraint (and associated information) for the table    * descriptor.    *     * @param desc    *          {@link HTableDescriptor} to modify    * @param clazz    *          {@link Constraint} class to remove    */
specifier|public
specifier|static
name|void
name|remove
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|clazz
parameter_list|)
block|{
name|String
name|key
init|=
name|serializeConstraintClass
argument_list|(
name|clazz
argument_list|)
decl_stmt|;
name|desc
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
comment|/**    * Enable the given {@link Constraint}. Retains all the information (e.g.    * Configuration) for the {@link Constraint}, but makes sure that it gets    * loaded on the table.    *     * @param desc    *          {@link HTableDescriptor} to modify    * @param clazz    *          {@link Constraint} to enable    * @throws IOException    *           If the constraint cannot be properly deserialized    */
specifier|public
specifier|static
name|void
name|enableConstraint
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|clazz
parameter_list|)
throws|throws
name|IOException
block|{
name|changeConstraintEnabled
argument_list|(
name|desc
argument_list|,
name|clazz
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Disable the given {@link Constraint}. Retains all the information (e.g.    * Configuration) for the {@link Constraint}, but it just doesn't load the    * {@link Constraint} on the table.    *     * @param desc    *          {@link HTableDescriptor} to modify    * @param clazz    *          {@link Constraint} to disable.    * @throws IOException    *           if the constraint cannot be found    */
specifier|public
specifier|static
name|void
name|disableConstraint
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|clazz
parameter_list|)
throws|throws
name|IOException
block|{
name|changeConstraintEnabled
argument_list|(
name|desc
argument_list|,
name|clazz
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Change the whether the constraint (if it is already present) is enabled or    * disabled.    */
specifier|private
specifier|static
name|void
name|changeConstraintEnabled
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|clazz
parameter_list|,
name|boolean
name|enabled
parameter_list|)
throws|throws
name|IOException
block|{
comment|// get the original constraint
name|Pair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
init|=
name|getKeyValueForClass
argument_list|(
name|desc
argument_list|,
name|clazz
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Constraint: "
operator|+
name|clazz
operator|.
name|getName
argument_list|()
operator|+
literal|" is not associated with this table. You can't enable it!"
argument_list|)
throw|;
block|}
comment|// create a new configuration from that conf
name|Configuration
name|conf
init|=
name|readConfiguration
argument_list|(
name|entry
operator|.
name|getSecond
argument_list|()
argument_list|)
decl_stmt|;
comment|// set that it is enabled
name|conf
operator|.
name|setBoolean
argument_list|(
name|ENABLED_KEY
argument_list|,
name|enabled
argument_list|)
expr_stmt|;
comment|// write it back out
name|writeConstraint
argument_list|(
name|desc
argument_list|,
name|entry
operator|.
name|getFirst
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check to see if the given constraint is enabled.    *     * @param desc    *          {@link HTableDescriptor} to check.    * @param clazz    *          {@link Constraint} to check for    * @return<tt>true</tt> if the {@link Constraint} is present and enabled.    *<tt>false</tt> otherwise.    * @throws IOException    *           If the constraint has improperly stored in the table    */
specifier|public
specifier|static
name|boolean
name|enabled
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|clazz
parameter_list|)
throws|throws
name|IOException
block|{
comment|// get the kv
name|Pair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
init|=
name|getKeyValueForClass
argument_list|(
name|desc
argument_list|,
name|clazz
argument_list|)
decl_stmt|;
comment|// its not enabled so just return false. In fact, its not even present!
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// get the info about the constraint
name|Configuration
name|conf
init|=
name|readConfiguration
argument_list|(
name|entry
operator|.
name|getSecond
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|ENABLED_KEY
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Get the constraints stored in the table descriptor    *     * @param desc    *          To read from    * @param classloader    *          To use when loading classes. If a special classloader is used on a    *          region, for instance, then that should be the classloader used to    *          load the constraints. This could also apply to unit-testing    *          situation, where want to ensure that class is reloaded or not.    * @return List of configured {@link Constraint Constraints}    * @throws IOException    *           if any part of reading/arguments fails    */
specifier|static
name|List
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|getConstraints
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|ClassLoader
name|classloader
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Constraint
argument_list|>
name|constraints
init|=
operator|new
name|ArrayList
argument_list|<
name|Constraint
argument_list|>
argument_list|()
decl_stmt|;
comment|// loop through all the key, values looking for constraints
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|ImmutableBytesWritable
argument_list|>
name|e
range|:
name|desc
operator|.
name|getValues
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// read out the constraint
name|String
name|key
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
name|String
index|[]
name|className
init|=
name|CONSTRAINT_HTD_ATTR_KEY_PATTERN
operator|.
name|split
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|className
operator|.
name|length
operator|==
literal|2
condition|)
block|{
name|key
operator|=
name|className
index|[
literal|1
index|]
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Loading constraint:"
operator|+
name|key
argument_list|)
expr_stmt|;
block|}
comment|// read in the rest of the constraint
name|Configuration
name|conf
decl_stmt|;
try|try
block|{
name|conf
operator|=
name|readConfiguration
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
comment|// long that we don't have a valid configuration stored, and move on.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Corrupted configuration found for key:"
operator|+
name|key
operator|+
literal|",  skipping it."
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// if it is not enabled, skip it
if|if
condition|(
operator|!
name|conf
operator|.
name|getBoolean
argument_list|(
name|ENABLED_KEY
argument_list|,
literal|false
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|debug
argument_list|(
literal|"Constraint: "
operator|+
name|key
operator|+
literal|" is DISABLED - skipping it"
argument_list|)
expr_stmt|;
comment|// go to the next constraint
continue|continue;
block|}
try|try
block|{
comment|// add the constraint, now that we expect it to be valid.
name|Class
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|clazz
init|=
name|classloader
operator|.
name|loadClass
argument_list|(
name|key
argument_list|)
operator|.
name|asSubclass
argument_list|(
name|Constraint
operator|.
name|class
argument_list|)
decl_stmt|;
name|Constraint
name|constraint
init|=
name|clazz
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|constraint
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|constraints
operator|.
name|add
argument_list|(
name|constraint
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e1
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e1
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InstantiationException
name|e1
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e1
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e1
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e1
argument_list|)
throw|;
block|}
block|}
block|}
comment|// sort them, based on the priorities
name|Collections
operator|.
name|sort
argument_list|(
name|constraints
argument_list|,
name|constraintComparator
argument_list|)
expr_stmt|;
return|return
name|constraints
return|;
block|}
specifier|private
specifier|static
specifier|final
name|Comparator
argument_list|<
name|Constraint
argument_list|>
name|constraintComparator
init|=
operator|new
name|Comparator
argument_list|<
name|Constraint
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Constraint
name|c1
parameter_list|,
name|Constraint
name|c2
parameter_list|)
block|{
comment|// compare the priorities of the constraints stored in their configuration
return|return
name|Long
operator|.
name|valueOf
argument_list|(
name|c1
operator|.
name|getConf
argument_list|()
operator|.
name|getLong
argument_list|(
name|PRIORITY_KEY
argument_list|,
name|DEFAULT_PRIORITY
argument_list|)
argument_list|)
operator|.
name|compareTo
argument_list|(
name|c2
operator|.
name|getConf
argument_list|()
operator|.
name|getLong
argument_list|(
name|PRIORITY_KEY
argument_list|,
name|DEFAULT_PRIORITY
argument_list|)
argument_list|)
return|;
block|}
block|}
decl_stmt|;
block|}
end_class

end_unit

