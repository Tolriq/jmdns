//Copyright 2003-2005 Arthur van Hoff, Rick Blair
//Licensed under Apache License version 2.0
//Original license LGPL

package javax.jmdns.impl;

import java.util.Set;

import javax.jmdns.ServiceInfo;
import javax.jmdns.impl.constants.DNSConstants;
import javax.jmdns.impl.constants.DNSRecordClass;
import javax.jmdns.impl.constants.DNSRecordType;

/**
 * A DNS question.
 *
 * @version %I%, %G%
 * @author Arthur van Hoff, Pierre Frisch
 */
public class DNSQuestion extends DNSEntry
{
    // private static Logger logger = Logger.getLogger(DNSQuestion.class.getName());

    /**
     * Address question.
     */
    private static class DNS4Address extends DNSQuestion
    {
        DNS4Address(String name, DNSRecordType type, DNSRecordClass recordClass, boolean unique)
        {
            super(name, type, recordClass, unique);
        }

        @Override
        public void addAnswers(JmDNSImpl jmDNSImpl, Set<DNSRecord> answers)
        {
            DNSRecord answer = jmDNSImpl.getLocalHost().getDNS4AddressRecord();
            if (answer != null)
            {
                answers.add(answer);
            }
        }

        @Override
        public boolean iAmTheOnlyOne(JmDNSImpl jmDNSImpl)
        {
            String name = this.getName().toLowerCase();
            return jmDNSImpl.getLocalHost().getName().equals(name) || jmDNSImpl.getServices().keySet().contains(name);
        }

    }

    /**
     * Address question.
     */
    private static class DNS6Address extends DNSQuestion
    {
        DNS6Address(String name, DNSRecordType type, DNSRecordClass recordClass, boolean unique)
        {
            super(name, type, recordClass, unique);
        }

        @Override
        public void addAnswers(JmDNSImpl jmDNSImpl, Set<DNSRecord> answers)
        {
            DNSRecord answer = jmDNSImpl.getLocalHost().getDNS6AddressRecord();
            if (answer != null)
            {
                answers.add(answer);
            }
        }

        @Override
        public boolean iAmTheOnlyOne(JmDNSImpl jmDNSImpl)
        {
            String name = this.getName().toLowerCase();
            return jmDNSImpl.getLocalHost().getName().equals(name) || jmDNSImpl.getServices().keySet().contains(name);
        }

    }

    /**
     * Host Information question.
     */
    private static class HostInformation extends DNSQuestion
    {
        HostInformation(String name, DNSRecordType type, DNSRecordClass recordClass, boolean unique)
        {
            super(name, type, recordClass, unique);
        }
    }

    /**
     * Pointer question.
     */
    private static class Pointer extends DNSQuestion
    {
        Pointer(String name, DNSRecordType type, DNSRecordClass recordClass, boolean unique)
        {
            super(name, type, recordClass, unique);
        }

        @Override
        public void addAnswers(JmDNSImpl jmDNSImpl, Set<DNSRecord> answers)
        {
            // find matching services
            for (ServiceInfo serviceInfo : jmDNSImpl.getServices().values())
            {
                this.addAnswersForServiceInfo(jmDNSImpl, answers, (ServiceInfoImpl) serviceInfo);
            }
            if (this.getName().equalsIgnoreCase("_services" + DNSConstants.DNS_META_QUERY + "local."))
            {
                for (String serviceType : jmDNSImpl.getServiceTypes().values())
                {
                    answers.add(new DNSRecord.Pointer("_services" + DNSConstants.DNS_META_QUERY + "local.", DNSRecordType.TYPE_PTR, DNSRecordClass.CLASS_IN, DNSRecordClass.NOT_UNIQUE, DNSConstants.DNS_TTL, serviceType));
                }
            }
        }

    }

    /**
     * Service question.
     */
    private static class Service extends DNSQuestion
    {
        Service(String name, DNSRecordType type, DNSRecordClass recordClass, boolean unique)
        {
            super(name, type, recordClass, unique);
        }

        @Override
        public void addAnswers(JmDNSImpl jmDNSImpl, Set<DNSRecord> answers)
        {
            String name = this.getName().toLowerCase();
            if (jmDNSImpl.getLocalHost().getName().equalsIgnoreCase(name))
            {
                // type = DNSConstants.TYPE_A;
                DNSRecord answer = jmDNSImpl.getLocalHost().getDNS4AddressRecord();
                if (answer != null)
                {
                    answers.add(answer);
                }
                answer = jmDNSImpl.getLocalHost().getDNS6AddressRecord();
                if (answer != null)
                {
                    answers.add(answer);
                }
                return;
            }
            // Service type request
            if (jmDNSImpl.getServiceTypes().containsKey(name))
            {
                DNSQuestion question = new Pointer(this.getName(), DNSRecordType.TYPE_PTR, this.getRecordClass(), this.isUnique());
                question.addAnswers(jmDNSImpl, answers);
                return;
            }

            this.addAnswersForServiceInfo(jmDNSImpl, answers, (ServiceInfoImpl) jmDNSImpl.getServices().get(name));
        }

        @Override
        public boolean iAmTheOnlyOne(JmDNSImpl jmDNSImpl)
        {
            String name = this.getName().toLowerCase();
            return jmDNSImpl.getLocalHost().getName().equals(name) || jmDNSImpl.getServices().keySet().contains(name);
        }

    }

    /**
     * Text question.
     */
    private static class Text extends DNSQuestion
    {
        Text(String name, DNSRecordType type, DNSRecordClass recordClass, boolean unique)
        {
            super(name, type, recordClass, unique);
        }

        @Override
        public void addAnswers(JmDNSImpl jmDNSImpl, Set<DNSRecord> answers)
        {
            this.addAnswersForServiceInfo(jmDNSImpl, answers, (ServiceInfoImpl) jmDNSImpl.getServices().get(this.getName().toLowerCase()));
        }

        @Override
        public boolean iAmTheOnlyOne(JmDNSImpl jmDNSImpl)
        {
            String name = this.getName().toLowerCase();
            return jmDNSImpl.getLocalHost().getName().equals(name) || jmDNSImpl.getServices().keySet().contains(name);
        }

    }

    /**
     * AllRecords question.
     */
    private static class AllRecords extends DNSQuestion
    {
        AllRecords(String name, DNSRecordType type, DNSRecordClass recordClass, boolean unique)
        {
            super(name, type, recordClass, unique);
        }

        @Override
        public boolean isSameType(DNSEntry entry)
        {
            // We match all non null entry
            return (entry != null);
        }

        @Override
        public void addAnswers(JmDNSImpl jmDNSImpl, Set<DNSRecord> answers)
        {
            System.err.println("DNSQuestion.AllRecords() name: " + this.getName());
            String name = this.getName().toLowerCase();
            if (jmDNSImpl.getLocalHost().getName().equalsIgnoreCase(name))
            {
                // type = DNSConstants.TYPE_A;
                DNSRecord answer = jmDNSImpl.getLocalHost().getDNS4AddressRecord();
                System.err.println("DNSQuestion.AllRecords() TYPE_A answer: " + answer);
                if (answer != null)
                {
                    answers.add(answer);
                }
                answer = jmDNSImpl.getLocalHost().getDNS6AddressRecord();
                System.err.println("DNSQuestion.AllRecords() TYPE_AAAA answer: " + answer);
                if (answer != null)
                {
                    answers.add(answer);
                }
                return;
            }
            // Service type request
            if (jmDNSImpl.getServiceTypes().containsKey(name))
            {
                DNSQuestion question = new Pointer(this.getName(), DNSRecordType.TYPE_PTR, this.getRecordClass(), this.isUnique());
                question.addAnswers(jmDNSImpl, answers);
                return;
            }

            this.addAnswersForServiceInfo(jmDNSImpl, answers, (ServiceInfoImpl) jmDNSImpl.getServices().get(name));
        }

        @Override
        public boolean iAmTheOnlyOne(JmDNSImpl jmDNSImpl)
        {
            String name = this.getName().toLowerCase();
            return jmDNSImpl.getLocalHost().getName().equals(name) || jmDNSImpl.getServices().keySet().contains(name);
        }

    }

    DNSQuestion(String name, DNSRecordType type, DNSRecordClass recordClass, boolean unique)
    {
        super(name, type, recordClass, unique);
    }

    /**
     * Create a question.
     *
     * @param name
     * @param type
     * @param recordClass
     * @param unique
     * @return new question
     */
    public static DNSQuestion newQuestion(String name, DNSRecordType type, DNSRecordClass recordClass, boolean unique)
    {
        switch (type)
        {
            case TYPE_A:
                return new DNS4Address(name, type, recordClass, unique);
            case TYPE_A6:
                return new DNS6Address(name, type, recordClass, unique);
            case TYPE_AAAA:
                return new DNS6Address(name, type, recordClass, unique);
            case TYPE_ANY:
                return new AllRecords(name, type, recordClass, unique);
            case TYPE_HINFO:
                return new HostInformation(name, type, recordClass, unique);
            case TYPE_PTR:
                return new Pointer(name, type, recordClass, unique);
            case TYPE_SRV:
                return new Service(name, type, recordClass, unique);
            case TYPE_TXT:
                return new Text(name, type, recordClass, unique);
            default:
                return new DNSQuestion(name, type, recordClass, unique);
        }
    }

    /**
     * Check if this question is answered by a given DNS record.
     */
    boolean answeredBy(DNSEntry rec)
    {
        return this.isSameRecordClass(rec) && this.isSameType(rec) && this.getName().equals(rec.getName());
    }

    /**
     * Adds answers to the list for our question.
     *
     * @param jmDNSImpl
     *            DNS holding the records
     * @param answers
     *            List of previous answer to append.
     */
    public void addAnswers(JmDNSImpl jmDNSImpl, Set<DNSRecord> answers)
    {
        // By default we do nothing
    }

    protected void addAnswersForServiceInfo(JmDNSImpl jmDNSImpl, Set<DNSRecord> answers, ServiceInfoImpl info)
    {
        System.err.println("addAnswersForServiceInfo() info: " + info);
        if ((info != null) && info.isAnnounced())
        {
            if (this.getName().equalsIgnoreCase(info.getType()))
            {
                DNSRecord answer = jmDNSImpl.getLocalHost().getDNS4AddressRecord();
                if (answer != null)
                {
                    answers.add(answer);
                }
                answer = jmDNSImpl.getLocalHost().getDNS6AddressRecord();
                if (answer != null)
                {
                    answers.add(answer);
                }
                answers.add(new DNSRecord.Pointer(info.getType(), DNSRecordType.TYPE_PTR, DNSRecordClass.CLASS_IN, DNSRecordClass.NOT_UNIQUE, DNSConstants.DNS_TTL, info.getQualifiedName()));
                answers.add(new DNSRecord.Service(info.getQualifiedName(), DNSRecordType.TYPE_SRV, DNSRecordClass.CLASS_IN, DNSRecordClass.UNIQUE, DNSConstants.DNS_TTL, info.getPriority(), info.getWeight(), info.getPort(), jmDNSImpl.getLocalHost()
                        .getName()));
                answers.add(new DNSRecord.Text(info.getQualifiedName(), DNSRecordType.TYPE_TXT, DNSRecordClass.CLASS_IN, DNSRecordClass.UNIQUE, DNSConstants.DNS_TTL, info.getText()));
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.jmdns.impl.DNSEntry#isExpired(long)
     */
    @Override
    boolean isExpired(long now)
    {
        return false;
    }

    /**
     * Checks if we are the only to be able to answer that question.
     *
     * @param jmDNSImpl
     *            DNS holding the records
     * @return <code>true</code> if we are the only one with the answer to the question, <code>false</code> otherwise.
     */
    public boolean iAmTheOnlyOne(JmDNSImpl jmDNSImpl)
    {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.jmdns.impl.DNSEntry#toString(java.lang.StringBuilder)
     */
    @Override
    public void toString(StringBuilder aLog)
    {
        // do nothing
    }

}
